/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

var App = require('app');
var date = require('utils/date/date');
var numberUtils = require('utils/number_utils');

App.MainDashboardServiceHbaseView = App.MainDashboardServiceView.extend({
  templateName: require('templates/main/service/services/hbase'),
  serviceName: 'hbase',
  /**
   * All master components
   */
  masters: Em.computed.filterBy('service.hostComponents', 'isMaster', true),
  /**
   * Passive master components
   */
  passiveMasters: Em.computed.filterBy('masters', 'haStatus', 'false'),

  regionServesText: Em.computed.countBasedMessage('service.regionServersTotal', '', Em.I18n.t('services.service.summary.viewHost'), Em.I18n.t('services.service.summary.viewHosts')),

  phoenixServersText: Em.computed.countBasedMessage('service.phoenixServersTotal', '', Em.I18n.t('services.service.summary.viewHost'), Em.I18n.t('services.service.summary.viewHosts')),

  showPhoenixInfo: Em.computed.bool('service.phoenixServersTotal'),

  /**
   * One(!) active master component
   */
  activeMaster: Em.computed.findBy('masters', 'haStatus', 'true'),

  activeMasterTitle: Em.I18n.t('service.hbase.activeMaster'),

  masterServerHeapSummary: App.MainDashboardServiceView.formattedHeap('dashboard.services.hbase.masterServerHeap.summary', 'service.heapMemoryUsed', 'service.heapMemoryMax'),

  summaryHeader: function () {
    var avgLoad = this.get('service.averageLoad');
    if (isNaN(avgLoad)) {
      avgLoad = this.t("services.service.summary.unknown");
    }
    return this.t("dashboard.services.hbase.summary").format(this.get('service.regionServersTotal'), avgLoad);
  }.property('service.regionServersTotal', 'service.averageLoad'),

  hbaseMasterWebUrl: function () {
    if (this.get('activeMaster.host') && this.get('activeMaster.host').get('publicHostName')) {
      return "http://" + (App.singleNodeInstall ? App.singleNodeAlias : this.get('activeMaster.host').get('publicHostName')) + ":60010";
    }
  }.property('activeMaster'),

  averageLoad: function () {
    var avgLoad = this.get('service.averageLoad');
    if (isNaN(avgLoad)) {
      avgLoad = this.t('services.service.summary.notAvailable');
    }
    return this.t('dashboard.services.hbase.averageLoadPerServer').format(avgLoad);
  }.property("service.averageLoad"),

  masterStartedTime: function () {
    var uptime = this.get('service').get('masterStartTime');
    if (uptime && uptime > 0) {
      var diff = App.dateTime() - uptime;
      if (diff < 0) {
        diff = 0;
      }
      var formatted = date.timingFormat(diff);
      return this.t('dashboard.services.uptime').format(formatted);
    }
    return this.t('services.service.summary.notRunning');
  }.property("service.masterStartTime"),

  masterActivatedTime: function () {
    var uptime = this.get('service').get('masterActiveTime');
    if (uptime && uptime > 0) {
      var diff = App.dateTime() - uptime;
      if (diff < 0) {
        diff = 0;
      }
      var formatted = date.timingFormat(diff);
      return this.t('dashboard.services.uptime').format(formatted);
    }
    return this.t('services.service.summary.notRunning');
  }.property("service.masterActiveTime"),

  regionServerComponent: Em.Object.create({
    componentName: 'HBASE_REGIONSERVER'
  }),

  phoenixServerComponent: Em.Object.create({
    componentName: 'PHOENIX_QUERY_SERVER'
  })
});
