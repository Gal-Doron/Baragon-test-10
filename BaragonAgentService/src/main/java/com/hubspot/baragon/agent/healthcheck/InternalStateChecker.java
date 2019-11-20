package com.hubspot.baragon.agent.healthcheck;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hubspot.baragon.agent.BaragonAgentServiceModule;
import com.hubspot.baragon.data.BaragonStateDatastore;
import com.hubspot.baragon.models.BaragonService;
import com.hubspot.baragon.models.BasicServiceContext;
import com.hubspot.baragon.models.UpstreamInfo;

@Singleton
public class InternalStateChecker implements Runnable {
  private final BaragonStateDatastore stateDatastore;
  private final Map<String, BasicServiceContext> internalStateCache;
  private final Set<String> stateErrors;

  @Inject
  public InternalStateChecker(BaragonStateDatastore stateDatastore,
                              @Named(BaragonAgentServiceModule.INTERNAL_STATE_CACHE) Map<String, BasicServiceContext> internalStateCache,
                              @Named(BaragonAgentServiceModule.LOCAL_STATE_ERROR_MESSAGE) Set<String> stateErrors) {
    this.stateDatastore = stateDatastore;
    this.internalStateCache = internalStateCache;
    this.stateErrors = stateErrors;
  }

  @Override
  public void run() {
    Set<String> invalidServiceMessages = new HashSet<>();
    long now = System.currentTimeMillis();
    internalStateCache.forEach((serviceId, context) -> {
      Optional<BaragonService> maybeService = stateDatastore.getService(serviceId);
      if (!maybeService.isPresent()) {
        invalidServiceMessages.add(String.format("%s no longer exists in state datastore, but exists in agent", serviceId));
        return;
      }
      Collection<UpstreamInfo> existingUpstreams = stateDatastore.getUpstreams(serviceId);
      BasicServiceContext datastoreContext = new BasicServiceContext(maybeService.get(), existingUpstreams);
      if (!datastoreContext.equals(context) && now - context.getTimestamp() > TimeUnit.SECONDS.toMillis(60)) {
        invalidServiceMessages.add(String.format("Agent context for %s does not match baragon state (Agent: %s, Service: %s)", serviceId, context, datastoreContext));
      }
    });
    stateErrors.clear();
    if (!invalidServiceMessages.isEmpty()) {
      stateErrors.addAll(invalidServiceMessages);
    }
  }
}
