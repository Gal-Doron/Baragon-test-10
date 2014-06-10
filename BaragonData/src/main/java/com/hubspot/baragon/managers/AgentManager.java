package com.hubspot.baragon.managers;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hubspot.baragon.BaragonBaseModule;
import com.hubspot.baragon.data.BaragonAgentResponseDatastore;
import com.hubspot.baragon.data.BaragonLoadBalancerDatastore;
import com.hubspot.baragon.models.AgentRequestType;
import com.hubspot.baragon.models.AgentRequestsStatus;
import com.hubspot.baragon.models.AgentResponse;
import com.hubspot.baragon.models.AgentResponseId;
import com.hubspot.baragon.models.BaragonRequest;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

@Singleton
public class AgentManager {
  private static final Log LOG = LogFactory.getLog(AgentManager.class);

  private final BaragonLoadBalancerDatastore loadBalancerDatastore;
  private final BaragonAgentResponseDatastore agentResponseDatastore;
  private final AsyncHttpClient asyncHttpClient;
  private final String baragonAgentRequestUriFormat;
  private final Integer baragonAgentMaxAttempts;

  @Inject
  public AgentManager(BaragonLoadBalancerDatastore loadBalancerDatastore,
                      BaragonAgentResponseDatastore agentResponseDatastore,
                      @Named(BaragonBaseModule.BARAGON_SERVICE_HTTP_CLIENT) AsyncHttpClient asyncHttpClient,
                      @Named(BaragonBaseModule.BARAGON_AGENT_REQUEST_URI_FORMAT) String baragonAgentRequestUriFormat,
                      @Named(BaragonBaseModule.BARAGON_AGENT_MAX_ATTEMPTS) Integer baragonAgentMaxAttempts) {
    this.loadBalancerDatastore = loadBalancerDatastore;
    this.agentResponseDatastore = agentResponseDatastore;
    this.asyncHttpClient = asyncHttpClient;
    this.baragonAgentRequestUriFormat = baragonAgentRequestUriFormat;
    this.baragonAgentMaxAttempts = baragonAgentMaxAttempts;
  }

  private AsyncHttpClient.BoundRequestBuilder buildAgentRequest(String url, AgentRequestType requestType) {
    switch (requestType) {
      case APPLY:
        return asyncHttpClient.preparePost(url);
      case REVERT:
      case CANCEL:
        return asyncHttpClient.prepareDelete(url);
      default:
        throw new RuntimeException("Don't know how to send requests for " + requestType);
    }
  }

  public void sendRequests(final BaragonRequest request, final AgentRequestType requestType) {
    final Collection<String> baseUrls = loadBalancerDatastore.getAllBaseUrls(request.getLoadBalancerService().getLoadBalancerGroups());
    for (final String baseUrl : baseUrls) {
      // wait until pending request has completed.
      if (agentResponseDatastore.hasPendingRequest(request.getLoadBalancerRequestId(), baseUrl)) {
        continue;
      }

      final Optional<AgentResponseId> maybeLastResponseId = agentResponseDatastore.getLastAgentResponseId(request.getLoadBalancerRequestId(), requestType, baseUrl);

      // don't retry request if we've hit the max attempts, or the request was successful
      if (maybeLastResponseId.isPresent() && (maybeLastResponseId.get().getAttempt() > baragonAgentMaxAttempts || maybeLastResponseId.get().isSuccess())) {
        continue;
      }

      agentResponseDatastore.setPendingRequestStatus(request.getLoadBalancerRequestId(), baseUrl, true);

      final String url = String.format(baragonAgentRequestUriFormat, baseUrl, request.getLoadBalancerRequestId());

      try {
        buildAgentRequest(url, requestType).execute(new AsyncCompletionHandler<Void>() {
          @Override
          public Void onCompleted(Response response) throws Exception {
            LOG.info(String.format("Got HTTP %d from %s for %s", response.getStatusCode(), baseUrl, request.getLoadBalancerRequestId()));
            final Optional<String> content = Strings.isNullOrEmpty(response.getResponseBody()) ? Optional.<String>absent() : Optional.of(response.getResponseBody());
            agentResponseDatastore.addAgentResponse(request.getLoadBalancerRequestId(), requestType, baseUrl, url, Optional.of(response.getStatusCode()), content, Optional.<String>absent());
            agentResponseDatastore.setPendingRequestStatus(request.getLoadBalancerRequestId(), baseUrl, false);
            return null;
          }

          @Override
          public void onThrowable(Throwable t) {
            LOG.info(String.format("Got exception %s when hitting %s for %s", t, baseUrl, request.getLoadBalancerRequestId()));
            agentResponseDatastore.addAgentResponse(request.getLoadBalancerRequestId(), requestType, baseUrl, url, Optional.<Integer>absent(), Optional.<String>absent(), Optional.of(t.getMessage()));
            agentResponseDatastore.setPendingRequestStatus(request.getLoadBalancerRequestId(), baseUrl, false);
          }
        });
      } catch (Exception e) {
        LOG.info(String.format("Got exception %s when hitting %s for %s", e, baseUrl, request.getLoadBalancerRequestId()));
        agentResponseDatastore.addAgentResponse(request.getLoadBalancerRequestId(), requestType, baseUrl, url, Optional.<Integer>absent(), Optional.<String>absent(), Optional.of(e.getMessage()));
        agentResponseDatastore.setPendingRequestStatus(request.getLoadBalancerRequestId(), baseUrl, false);
      }
    }
  }

  public AgentRequestsStatus getRequestsStatus(BaragonRequest request, AgentRequestType requestType) {
    final Collection<String> baseUrls = loadBalancerDatastore.getAllBaseUrls(request.getLoadBalancerService().getLoadBalancerGroups());

    boolean success = true;

    for (String baseUrl : baseUrls) {
      final Optional<AgentResponseId> maybeAgentResponseId = agentResponseDatastore.getLastAgentResponseId(request.getLoadBalancerRequestId(), requestType, baseUrl);

      if (!maybeAgentResponseId.isPresent() || agentResponseDatastore.hasPendingRequest(request.getLoadBalancerRequestId(), baseUrl)) {
        return AgentRequestsStatus.WAITING;
      }

      final AgentResponseId agentResponseId = maybeAgentResponseId.get();

      if ((agentResponseId.getAttempt() < baragonAgentMaxAttempts - 1) && !agentResponseId.isSuccess()) {
        return AgentRequestsStatus.RETRY;
      } else {
        success = success && agentResponseId.isSuccess();
      }
    }

    return success ? AgentRequestsStatus.SUCCESS : AgentRequestsStatus.FAILURE;
  }

  public Map<AgentRequestType, Collection<AgentResponse>> getAgentResponses(String requestId) {
    return agentResponseDatastore.getLastResponses(requestId);
  }
}