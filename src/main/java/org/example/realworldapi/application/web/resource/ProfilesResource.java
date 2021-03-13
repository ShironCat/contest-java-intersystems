package org.example.realworldapi.application.web.resource;

import lombok.AllArgsConstructor;
import org.example.realworldapi.application.utils.ResourceUtils;
import org.example.realworldapi.domain.feature.FollowUserByUsername;
import org.example.realworldapi.domain.feature.UnfollowUserByUsername;
import org.example.realworldapi.domain.model.constants.ValidationMessages;
import org.example.realworldapi.infrastructure.web.security.annotation.Secured;
import org.example.realworldapi.infrastructure.web.security.profile.Role;

import javax.transaction.Transactional;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/profiles")
@AllArgsConstructor
public class ProfilesResource {

  private final FollowUserByUsername followUserByUsername;
  private final UnfollowUserByUsername unfollowUserByUsername;
  private final ResourceUtils resourceUtils;

  @GET
  @Secured(optional = true)
  @Path("/{username}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getProfile(
      @PathParam("username") @NotBlank(message = ValidationMessages.USERNAME_MUST_BE_NOT_BLANK)
          String username,
      @Context SecurityContext securityContext) {
    final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
    final var profileResponse = resourceUtils.profileResponse(username, loggedUserId);
    return Response.ok(profileResponse).status(Response.Status.OK).build();
  }

  @POST
  @Transactional
  @Secured({Role.USER, Role.ADMIN})
  @Path("/{username}/follow")
  @Produces(MediaType.APPLICATION_JSON)
  public Response follow(
      @PathParam("username") @NotBlank(message = ValidationMessages.USERNAME_MUST_BE_NOT_BLANK)
          String username,
      @Context SecurityContext securityContext) {
    final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
    followUserByUsername.handle(loggedUserId, username);
    return Response.ok(resourceUtils.profileResponse(username, loggedUserId))
        .status(Response.Status.OK)
        .build();
  }

  @DELETE
  @Transactional
  @Secured({Role.USER, Role.ADMIN})
  @Path("/{username}/follow")
  @Produces(MediaType.APPLICATION_JSON)
  public Response unfollow(
      @PathParam("username") @NotBlank(message = ValidationMessages.USERNAME_MUST_BE_NOT_BLANK)
          String username,
      @Context SecurityContext securityContext) {
    final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
    unfollowUserByUsername.handle(loggedUserId, username);
    return Response.ok(resourceUtils.profileResponse(username, loggedUserId))
        .status(Response.Status.OK)
        .build();
  }
}
