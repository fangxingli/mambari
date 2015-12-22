/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


var App = require('app');

describe('App.MainController', function () {
  var mainController = App.MainController.create();

  describe('#getServerVersionSuccessCallback', function () {

    var controller = App.MainController.create(),
      cases = [
        {
          osFamily: 'redhat5',
          expected: false
        },
        {
          osFamily: 'redhat6',
          expected: true
        },
        {
          osFamily: 'suse11',
          expected: false
        }
      ],
      title = 'App.isManagedMySQLForHiveEnabled should be {0} for {1}';

    cases.forEach(function (item) {
      it(title.format(item.expected, item.osFamily), function () {
        controller.getServerVersionSuccessCallback({
          'RootServiceComponents': {
            'component_version': '',
            'properties': {
              'server.os_family': item.osFamily
            }
          }
        });
        expect(App.get('isManagedMySQLForHiveEnabled')).to.equal(item.expected);
      });
    });
  });

  App.TestAliases.testAsComputedAlias(mainController, 'isClusterDataLoaded', 'App.router.clusterController.isLoaded', 'boolean');

  App.TestAliases.testAsComputedAlias(mainController, 'clusterDataLoadedPercent', 'App.router.clusterController.clusterDataLoadedPercent', 'string');

  describe('#initialize', function() {
    var initialize = false;
    beforeEach(function () {
      sinon.stub(App.router, 'get').returns({
        loadClusterData: function() {
          initialize = true;
        }
      });
    });
    afterEach(function () {
      App.router.get.restore();
    });
    it ('Should return true', function() {
      mainController.initialize();
      expect(initialize).to.be.true;
    });
  });

  describe('#dataLoading', function() {
    it ('Should resolve promise', function() {
      sinon.stub(App.router, 'get').returns(true);
      var deffer = mainController.dataLoading();
      App.router.get.restore();
      deffer.then(function(val){
        expect(val).to.be.undefined;
      });
    });
    it ('Should resolve promise', function() {
      sinon.stub(App.router, 'get').returns(false);
      
      setTimeout(function() {
        mainController.set('isClusterDataLoaded', true);
      },150);

      var deffer = mainController.dataLoading();
      App.router.get.restore();
      deffer.then(function(val){
        expect(val).to.be.undefined;
      });
    });
  });

  describe('#checkServerClientVersion', function() {
    var initialize = false;
    beforeEach(function () {
      sinon.stub(mainController, 'getServerVersion').returns({
        done: function(func) {
          if (func) {
            func();
          }
        }
      });
    });
    afterEach(function () {
      mainController.getServerVersion.restore();
    });
    it ('Should resolve promise', function() {
      var deffer = mainController.checkServerClientVersion();
      deffer.then(function(val){
        expect(val).to.be.undefined;
      });
    });
  });

  describe('#getServerVersion', function() {
    var res;
    beforeEach(function () {
      sinon.stub(App.ajax, 'send', function(data) {
        res = JSON.parse(JSON.stringify(data));
      });
    });
    afterEach(function () {
      App.ajax.send.restore();
    });
    it ('Should send data', function() {
      mainController.getServerVersion();
      expect(res).to.be.eql({
        "name": "ambari.service",
        "sender": {},
        "data": {
          "fields": "?fields=RootServiceComponents/component_version,RootServiceComponents/properties/server.os_family&minimal_response=true"
        },
        "success": "getServerVersionSuccessCallback",
        "error": "getServerVersionErrorCallback"
      });
    });
  });

  describe('#updateTitle', function() {
    beforeEach(function () {
      sinon.stub(App.router, 'get', function(message){
        if (message == 'clusterController.clusterName') {
          return 'c1';
        } else if (message == 'clusterInstallCompleted') {
          return true;
        } else if (message == 'clusterController') {
          return {
            get: function() {
              return true;
            }
          };
        }
      });
    });
    afterEach(function () {
      App.router.get.restore();
    });
    it ('Should update title', function() {
      $('body').append('<title id="title-id">text</title>');
      mainController.updateTitle();
      expect($('title').text()).to.be.equal('Ambari - c1');
      $('body').remove('#title-id');
    });
  });

});
