import React, { PropTypes } from 'react';
import { connect } from 'react-redux';

import { refresh } from '../../actions/ui/albDetail';
import Utils from '../../utils';
import rootComponent from '../../rootComponent';

import JSONButton from '../common/JSONButton';
import DetailGroup from '../common/DetailGroup';

import ListenerPanel from './ListenerPanel';

const AlbDetail = ({loadBalancer, listeners, targetGroupsArnsToNames}) => {
  const {
    loadBalancerName: name,
    availabilityZones: azs,
    securityGroups
  } = loadBalancer;
  return (
    <div>
      <div className="row detail-header">
        <div className="col-md-8">
          <h3>{ name }</h3>
        </div>
        <div className="col-md-4 button-container">
          <JSONButton object={loadBalancer}>
            <span className="btn btn-default">JSON</span>
          </JSONButton>
        </div>
      </div>
      <div className="row">
        <div className="col-md-12">
          <ul className="list-group">
            <li className="list-group-item"><strong>Hosted Zone ID:</strong> {loadBalancer.canonicalHostedZoneId}</li>
            <li className="list-group-item"><strong>VPC ID:</strong> {loadBalancer.vpcId}</li>
            <li className="list-group-item"><strong>Scheme:</strong> {loadBalancer.scheme}</li>
            <li className="list-group-item"><strong>Created:</strong> {Utils.timestampFromNow(loadBalancer.createdTime)}</li>
          </ul>
        </div>
      </div>
      <div className="row">
        <DetailGroup
          name="Availibility Zones"
          items={azs}
          keyGetter={(zone) => zone.zoneName}
          field={(zone) => (
            <ul className="list-unstyled">
              <li><strong>Zone:</strong> {zone.zoneName}</li>
              <li><strong>Subnet:</strong> {zone.subnetId}</li>
            </ul>
          )}
        />
        <DetailGroup name="Security Groups" items={securityGroups} />
        <ListenerPanel
          listeners={listeners}
          targetGroupsMap={targetGroupsArnsToNames}
        />
      </div>
    </div>
  );
};

AlbDetail.propTypes = {
  loadBalancer: PropTypes.object,
  listeners: PropTypes.array,
  targetGroupsArnsToNames: PropTypes.object,
};

const buildTargetGroupMap = (targetGroups) => {
  const map = {};
  targetGroups.forEach(({targetGroupArn, targetGroupName}) => {
    map[targetGroupArn] = targetGroupName;
  });
  return map;
};


const mapStateToProps = (state, ownProps) => ({
  loadBalancer: Utils.maybe(state, ['api', 'loadBalancer', ownProps.params.albName, 'data']),
  listeners: Utils.maybe(state, ['api', 'loadBalancerListeners', ownProps.params.albName, 'data']),
  targetGroupsArnsToNames: buildTargetGroupMap(Utils.maybe(state, ['api', 'targetGroups', 'data']))
});

export default connect(mapStateToProps)(rootComponent(AlbDetail, (props) => {
  return refresh(props.params.albName);
}));
