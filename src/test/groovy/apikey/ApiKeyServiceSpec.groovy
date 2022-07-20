/*
 * Copyright (C) 2022 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 */

package apikey

import grails.test.hibernate.HibernateSpec
import grails.testing.services.ServiceUnitTest

class ApiKeyServiceSpec extends HibernateSpec implements ServiceUnitTest<ApiKeyService> {

    def setup() {
        service.transactionManager = transactionManager
    }

    def cleanup() {
    }

    List<Class> getDomainClasses() { [APIKey, App] }

    void "test enable key"() {
        when:
        def app = new App(name: 'a', userEmail: 'a@b.net', userId: '1234', dateCreated: new Date())
        App.saveAll(app)
//        app.save()
        def key = new APIKey(apikey: '123456', userEmail: 'a@b.net', userId: '1234', app: app, enabled: false, dateCreated: new Date())
        APIKey.saveAll(key)
//        key.save()

        then:
        App.count == 1
        APIKey.count == 1

        when:
        def enabledKey = service.enableKey('123456', true)

        then:
        enabledKey.apikey == '123456'
        enabledKey.enabled == true
    }

    void "test validate key"() {
        when:
        def app = new App(name: 'a', userEmail: 'a@b.net', userId: '1234', dateCreated: new Date())
        App.saveAll(app)
//        app.save()
        def lastUsed = new Date() - 1
        def key = new APIKey(apikey: '123456', userEmail: 'a@b.net', userId: '1234', app: app, enabled: false, dateCreated: new Date(), lastUsed: lastUsed)
        APIKey.saveAll(key)
//        key.save()

        then:
        App.count == 1
        APIKey.count == 1

        when:
        def enabledKey = service.validateKey('123456', 'ABCD:ABCD:ABCD:ABCD:ABCD:ABCD:192.168.158.190')

        then:
        enabledKey.apikey == '123456'
        enabledKey.lastRemoteAddr == 'ABCD:ABCD:ABCD:ABCD:ABCD:ABCD:192.168.158.190'
        enabledKey.lastUsed > lastUsed

        when: "non existant key is used"
        def invalidKey = service.validateKey('123457', '127.0.0.1')

        then: "validateKey returns null"
        invalidKey == null
    }

    void "test find all keys"() {
        when:
        def apps = [
                new App(name: 'a', userEmail: 'a@b.net', userId: '1234', dateCreated: new Date())
                ,new App(name: 'b', userEmail: 'b@b.net', userId: '1235', dateCreated: new Date())
        ]
        App.saveAll(apps)

        def keys = [
                new APIKey(apikey: '123456', userEmail: 'a@b.net', userId: '1234', app: apps[0], enabled: true, dateCreated: new Date())
                ,new APIKey(apikey: '123457', userEmail: 'a@b.net', userId: '1234', app: apps[0], enabled: false, dateCreated: new Date())
                ,new APIKey(apikey: '123458', userEmail: 'b@b.net', userId: '1235', app: apps[1], enabled: true, dateCreated: new Date())
                ,new APIKey(apikey: '123459', userEmail: 'b@b.net', userId: '1235', app: apps[1], enabled: false, dateCreated: new Date())
        ]
        APIKey.saveAll(keys)

        then:
        App.count == 2
        APIKey.count == 4

        when:
        def found = service.findAllKeys('enabled', 'desc', 2, 2)

        then:
        found.size() == 2
        // found.totalCount == 4 // TODO GORM Hibernate Bug with non-MySQL databases: https://github.com/grails/gorm-hibernate5/issues/297
        found*.apikey.containsAll(['123457', '123459'])
    }
}
