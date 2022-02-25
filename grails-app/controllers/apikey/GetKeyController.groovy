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

import au.org.ala.web.AlaSecured
import au.org.ala.web.CASRoles

class GetKeyController {

    def index() { }

    LocalAuthService localAuthService

    @AlaSecured(value = CASRoles.ROLE_ADMIN, statusCode = 403, view = '/getKey/notCreated')
    def submit() {
        final userDetails = localAuthService.userDetails()
        final userId = userDetails[1]
        final userEmail = userDetails[0]
        APIKey key = new APIKey()
        key.apikey = UUID.randomUUID().toString()
        key.userId = userId
        key.userEmail = userEmail
        key.enabled = true
        App app = App.findByName(params.appName)
        key.app = app
        key.save(validate: true, flush: true)

        if (key.hasErrors()) {
            log.error("Creating apikey failed due to an error userId={} userEmail={} app={}", userId, userEmail, params.appName)
            key.errors.each { log.error(it) }
        } else {
            log.info("Created a new apikey apikey={} userId={} userEmail={} app={}", key.apikey, userId, userEmail, params.appName)
            render(view: "created", model: [key: key])
        }
    }
}
