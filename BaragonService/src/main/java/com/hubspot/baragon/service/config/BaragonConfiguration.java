package com.hubspot.baragon.service.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.hubspot.baragon.config.AuthConfiguration;
import com.hubspot.baragon.config.GraphiteConfiguration;
import com.hubspot.baragon.config.HttpClientConfiguration;
import com.hubspot.baragon.config.ZooKeeperConfiguration;
import io.dropwizard.Configuration;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BaragonConfiguration extends Configuration {
  public static final String DEFAULT_AGENT_REQUEST_URI_FORMAT = "%s/request/%s";
  public static final String DEFAULT_AGENT_BATCH_REQUEST_URI_FORMAT = "%s/batch";
  public static final String DEFAULT_AGENT_PURGE_CACHE_REQUEST_URI_FORMAT =
    "%s/purgeCache/%s";

  @JsonProperty("zookeeper")
  @NotNull
  @Valid
  private ZooKeeperConfiguration zooKeeperConfiguration;

  @JsonProperty("httpClient")
  @NotNull
  @Valid
  private HttpClientConfiguration httpClientConfiguration = new HttpClientConfiguration();

  @JsonProperty("workerIntervalMs")
  @Deprecated
  private Long workerIntervalMs = null;

  @JsonProperty("worker")
  @NotNull
  @Valid
  private BaragonWorkerConfiguration workerConfiguration = new BaragonWorkerConfiguration();

  @JsonProperty("agentRequestUriFormat")
  @NotEmpty
  private String agentRequestUriFormat = DEFAULT_AGENT_REQUEST_URI_FORMAT;

  @JsonProperty("agentBatchRequestUriFormat")
  @NotEmpty
  private String agentBatchRequestUriFormat = DEFAULT_AGENT_BATCH_REQUEST_URI_FORMAT;

  @JsonProperty("agentMaxAttempts")
  @Min(1)
  private int agentMaxAttempts = 5;

  @JsonProperty("agentRequestTimeoutMs")
  @Min(10000)
  private long agentRequestTimeoutMs = 60000;

  @JsonProperty("auth")
  @NotNull
  @Valid
  private AuthConfiguration authConfiguration = new AuthConfiguration();

  @JsonProperty("purgeCache")
  private PurgeCacheConfiguration purgeCacheConfiguration;

  @JsonProperty("hostname")
  private String hostname;

  @JsonProperty("masterAuthKey")
  private String masterAuthKey;

  @JsonProperty("elb")
  private Optional<ElbConfiguration> elb = Optional.absent();

  @JsonProperty("edgeCache")
  private EdgeCacheConfiguration edgeCache = new EdgeCacheConfiguration();

  @JsonProperty("ui")
  @Valid
  private UIConfiguration uiConfiguration = new UIConfiguration();

  @JsonProperty("enableCorsFilter")
  private boolean enableCorsFilter = false;

  @JsonProperty("history")
  private HistoryConfiguration historyConfiguration = new HistoryConfiguration();

  @JsonProperty("enforceTargetAgentCount")
  private boolean enforceTargetAgentCount = false;

  @JsonProperty("defaultTargetAgentCount")
  private int defaultTargetAgentCount = 1;

  @JsonProperty("graphite")
  private GraphiteConfiguration graphiteConfiguration = new GraphiteConfiguration();

  @JsonProperty("sentry")
  private Optional<SentryConfiguration> sentryConfiguration = Optional.absent();

  @JsonProperty("gcloud")
  private GoogleCloudConfiguration googleCloudConfiguration = new GoogleCloudConfiguration();

  @JsonProperty
  private long maxResolveCacheSize = 4000;

  @JsonProperty
  private long expireResolveCacheAfterDays = 30;

  @JsonProperty("agentPurgeCacheRequestUriFormat")
  private String agentPurgeCacheRequestUriFormat =
    DEFAULT_AGENT_PURGE_CACHE_REQUEST_URI_FORMAT;

  private int maxConcurrentRequestsPerAgent = 3;

  public ZooKeeperConfiguration getZooKeeperConfiguration() {
    return zooKeeperConfiguration;
  }

  public HttpClientConfiguration getHttpClientConfiguration() {
    return httpClientConfiguration;
  }

  public void setHttpClientConfiguration(
    HttpClientConfiguration httpClientConfiguration
  ) {
    this.httpClientConfiguration = httpClientConfiguration;
  }

  public void setZooKeeperConfiguration(ZooKeeperConfiguration zooKeeperConfiguration) {
    this.zooKeeperConfiguration = zooKeeperConfiguration;
  }

  public String getAgentRequestUriFormat() {
    return agentRequestUriFormat;
  }

  public void setAgentBatchRequestUriFormat(String agentBatchRequestUriFormat) {
    this.agentBatchRequestUriFormat = agentBatchRequestUriFormat;
  }

  public String getAgentBatchRequestUriFormat() {
    return agentBatchRequestUriFormat;
  }

  public void setAgentRequestUriFormat(String agentRequestUriFormat) {
    this.agentRequestUriFormat = agentRequestUriFormat;
  }

  public Optional<Long> getWorkerIntervalMs() {
    return Optional.fromNullable(workerIntervalMs);
  }

  public void setWorkerIntervalMs(Long workerIntervalMs) {
    this.workerIntervalMs = workerIntervalMs;
  }

  public int getAgentMaxAttempts() {
    return agentMaxAttempts;
  }

  public long getAgentRequestTimeoutMs() {
    return agentRequestTimeoutMs;
  }

  public void setAgentMaxAttempts(int agentMaxAttempts) {
    this.agentMaxAttempts = agentMaxAttempts;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public AuthConfiguration getAuthConfiguration() {
    return authConfiguration;
  }

  public void setAuthConfiguration(AuthConfiguration authConfiguration) {
    this.authConfiguration = authConfiguration;
  }

  public PurgeCacheConfiguration getPurgeCacheConfiguration() {
    return purgeCacheConfiguration;
  }

  public void setPurgeCacheConfiguration(
    PurgeCacheConfiguration purgeCacheConfiguration
  ) {
    this.purgeCacheConfiguration = purgeCacheConfiguration;
  }

  public String getMasterAuthKey() {
    return masterAuthKey;
  }

  public void setMasterAuthKey(String masterAuthKey) {
    this.masterAuthKey = masterAuthKey;
  }

  public BaragonWorkerConfiguration getWorkerConfiguration() {
    return workerConfiguration;
  }

  public void setWorkerConfiguration(BaragonWorkerConfiguration workerConfiguration) {
    this.workerConfiguration = workerConfiguration;
  }

  public Optional<ElbConfiguration> getElbConfiguration() {
    return elb;
  }

  public void setElbConfiguration(Optional<ElbConfiguration> elb) {
    this.elb = elb;
  }

  public EdgeCacheConfiguration getEdgeCacheConfiguration() {
    return edgeCache;
  }

  public void setEdgeCacheConfiguration(EdgeCacheConfiguration edgeCache) {
    this.edgeCache = edgeCache;
  }

  public UIConfiguration getUiConfiguration() {
    return uiConfiguration;
  }

  public void setUiConfiguration(UIConfiguration uiConfiguration) {
    this.uiConfiguration = uiConfiguration;
  }

  public boolean isEnableCorsFilter() {
    return enableCorsFilter;
  }

  public HistoryConfiguration getHistoryConfiguration() {
    return historyConfiguration;
  }

  public void setHistoryConfiguration(HistoryConfiguration historyConfiguration) {
    this.historyConfiguration = historyConfiguration;
  }

  public boolean isEnforceTargetAgentCount() {
    return enforceTargetAgentCount;
  }

  public void setEnforceTargetAgentCount(boolean enforceTargetAgentCount) {
    this.enforceTargetAgentCount = enforceTargetAgentCount;
  }

  public int getDefaultTargetAgentCount() {
    return defaultTargetAgentCount;
  }

  public void setDefaultTargetAgentCount(int defaultTargetAgentCount) {
    this.defaultTargetAgentCount = defaultTargetAgentCount;
  }

  public GraphiteConfiguration getGraphiteConfiguration() {
    return graphiteConfiguration;
  }

  public void setGraphiteConfiguration(GraphiteConfiguration graphiteConfiguration) {
    this.graphiteConfiguration = graphiteConfiguration;
  }

  public Optional<SentryConfiguration> getSentryConfiguration() {
    return sentryConfiguration;
  }

  public void setSentryConfiguration(Optional<SentryConfiguration> sentryConfiguration) {
    this.sentryConfiguration = sentryConfiguration;
  }

  public GoogleCloudConfiguration getGoogleCloudConfiguration() {
    return googleCloudConfiguration;
  }

  public void setGoogleCloudConfiguration(
    GoogleCloudConfiguration googleCloudConfiguration
  ) {
    this.googleCloudConfiguration = googleCloudConfiguration;
  }

  public long getMaxResolveCacheSize() {
    return maxResolveCacheSize;
  }

  public void setMaxResolveCacheSize(long maxResolveCacheSize) {
    this.maxResolveCacheSize = maxResolveCacheSize;
  }

  public long getExpireResolveCacheAfterDays() {
    return expireResolveCacheAfterDays;
  }

  public void setExpireResolveCacheAfterDays(long expireResolveCacheAfterDays) {
    this.expireResolveCacheAfterDays = expireResolveCacheAfterDays;
  }

  public String getAgentPurgeCacheRequestUriFormat() {
    return agentPurgeCacheRequestUriFormat;
  }

  public void setAgentPurgeCacheRequestUriFormat(String agentPurgeCacheRequestUriFormat) {
    this.agentPurgeCacheRequestUriFormat = agentPurgeCacheRequestUriFormat;
  }

  public int getMaxConcurrentRequestsPerAgent() {
    return maxConcurrentRequestsPerAgent;
  }

  public void setMaxConcurrentRequestsPerAgent(int maxConcurrentRequestsPerAgent) {
    this.maxConcurrentRequestsPerAgent = maxConcurrentRequestsPerAgent;
  }
}
