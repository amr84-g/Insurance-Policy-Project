/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.insurance.policy.rest;

import com.axelor.apps.insurance.policy.db.ActivityTable;
import com.axelor.apps.insurance.policy.rest.dto.InsuranceActivityRequest;
import com.axelor.apps.insurance.policy.service.InsuranceIntegrationService;
import com.axelor.inject.Beans;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/insurance/activity")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class InsuranceIntegrationController {

  @POST
  public Response receiveActivity(InsuranceActivityRequest request) {
    try {
      ActivityTable activity =
          Beans.get(InsuranceIntegrationService.class).receiveActivity(request);
      return Response.status(Response.Status.CREATED)
          .entity(
              Map.of("policyId", activity.getPolicyId(), "activityId", activity.getActivityId()))
          .build();
    } catch (IllegalArgumentException exception) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(Map.of("error", exception.getMessage()))
          .build();
    }
  }
}
