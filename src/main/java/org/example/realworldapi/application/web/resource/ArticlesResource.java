package org.example.realworldapi.application.web.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.example.realworldapi.application.utils.ResourceUtils;
import org.example.realworldapi.application.web.model.request.NewArticleRequest;
import org.example.realworldapi.application.web.model.request.UpdateArticleRequest;
import org.example.realworldapi.domain.feature.*;
import org.example.realworldapi.domain.model.article.ArticleFilter;
import org.example.realworldapi.domain.model.constants.ValidationMessages;
import org.example.realworldapi.domain.service.ArticlesService;
import org.example.realworldapi.infrastructure.web.qualifiers.NoWrapRootValueObjectMapper;
import org.example.realworldapi.infrastructure.web.security.annotation.Secured;
import org.example.realworldapi.infrastructure.web.security.profile.Role;

import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@Path("/articles")
@AllArgsConstructor
public class ArticlesResource {

  private final FindArticlesByFilter findArticlesByFilter;
  private final CreateArticle createArticle;
  private final FindMostRecentArticlesByFilter findMostRecentArticlesByFilter;
  private final FindArticleBySlug findArticleBySlug;
  private final UpdateArticleBySlug updateArticleBySlug;
  private final DeleteArticleBySlug deleteArticleBySlug;
  private final ArticlesService articlesService;
  @NoWrapRootValueObjectMapper ObjectMapper objectMapper;
  private final ResourceUtils resourceUtils;

  @GET
  @Path("/feed")
  @Secured({Role.USER, Role.ADMIN})
  @Produces(MediaType.APPLICATION_JSON)
  public Response feed(
      @QueryParam("offset") int offset,
      @QueryParam("limit") int limit,
      @Context SecurityContext securityContext)
      throws JsonProcessingException {
    final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
    final var articlesFilter = new ArticleFilter(offset, limit, loggedUserId, null, null, null);
    final var articlesPageResult = findMostRecentArticlesByFilter.handle(articlesFilter);
    return Response.ok(
            objectMapper.writeValueAsString(
                resourceUtils.articlesResponse(articlesPageResult, loggedUserId)))
        .status(Response.Status.OK)
        .build();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Secured(optional = true)
  public Response getArticles(
      @QueryParam("offset") int offset,
      @QueryParam("limit") int limit,
      @QueryParam("tag") List<String> tags,
      @QueryParam("author") List<String> authors,
      @QueryParam("favorited") List<String> favorited,
      @Context SecurityContext securityContext)
      throws JsonProcessingException {
    final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
    final var filter = new ArticleFilter(offset, limit, loggedUserId, tags, authors, favorited);
    final var articlesPageResult = findArticlesByFilter.handle(filter);
    return Response.ok(
            objectMapper.writeValueAsString(
                resourceUtils.articlesResponse(articlesPageResult, loggedUserId)))
        .status(Response.Status.OK)
        .build();
  }

  @POST
  @Transactional
  @Secured({Role.ADMIN, Role.USER})
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response create(
      @Valid @NotNull(message = ValidationMessages.REQUEST_BODY_MUST_BE_NOT_NULL)
          NewArticleRequest newArticleRequest,
      @Context SecurityContext securityContext) {
    final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
    final var article = createArticle.handle(newArticleRequest.toNewArticleInput(loggedUserId));
    return Response.ok(resourceUtils.articleResponse(article, loggedUserId))
        .status(Response.Status.CREATED)
        .build();
  }

  @GET
  @Path("/{slug}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response findBySlug(
      @PathParam("slug") @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK)
          String slug) {
    final var article = findArticleBySlug.handle(slug);
    return Response.ok(resourceUtils.articleResponse(article, null))
        .status(Response.Status.OK)
        .build();
  }

  @PUT
  @Transactional
  @Path("/{slug}")
  @Secured({Role.ADMIN, Role.USER})
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response update(
      @PathParam("slug") @NotBlank String slug,
      @Valid @NotNull UpdateArticleRequest updateArticleRequest,
      @Context SecurityContext securityContext) {
    final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
    final var updatedArticle =
        updateArticleBySlug.handle(updateArticleRequest.toUpdateArticleInput(loggedUserId, slug));
    return Response.ok(resourceUtils.articleResponse(updatedArticle, null))
        .status(Response.Status.OK)
        .build();
  }

  @DELETE
  @Transactional
  @Path("/{slug}")
  @Secured({Role.ADMIN, Role.USER})
  @Produces(MediaType.APPLICATION_JSON)
  public Response delete(
      @PathParam("slug") @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String slug,
      @Context SecurityContext securityContext) {
    final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
    deleteArticleBySlug.handle(loggedUserId, slug);
    return Response.ok().build();
  }

  //  @GET
  //  @Path("/{slug}/comments")
  //  @Secured(optional = true)
  //  @Produces(MediaType.APPLICATION_JSON)
  //  public Response getCommentsBySlug(
  //      @PathParam("slug") @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String
  // slug,
  //      @Context SecurityContext securityContext)
  //      throws JsonProcessingException {
  //    Long loggedUserId = getLoggedUserId(securityContext);
  //    List<CommentData> comments = articlesService.findCommentsBySlug(slug, loggedUserId);
  //    return Response.ok(objectMapper.writeValueAsString(new CommentsResponse(comments)))
  //        .status(Response.Status.OK)
  //        .build();
  //  }

  //  @POST
  //  @Path("/{slug}/comments")
  //  @Secured({Role.ADMIN, Role.USER})
  //  @Consumes(MediaType.APPLICATION_JSON)
  //  @Produces(MediaType.APPLICATION_JSON)
  //  public Response createComment(
  //      @PathParam("slug") @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String
  // slug,
  //      @Valid NewCommentRequest newCommentRequest,
  //      @Context SecurityContext securityContext) {
  //    Long loggedUserId = getLoggedUserId(securityContext);
  //    CommentData commentData =
  //        articlesService.createComment(slug, newCommentRequest.getBody(), loggedUserId);
  //    return Response.ok(new CommentResponse(commentData)).status(Response.Status.OK).build();
  //  }

  //  @DELETE
  //  @Path("/{slug}/comments/{id}")
  //  @Secured({Role.ADMIN, Role.USER})
  //  @Produces(MediaType.APPLICATION_JSON)
  //  public Response deleteComment(
  //      @PathParam("slug") @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String
  // slug,
  //      @PathParam("id") @NotNull(message = ValidationMessages.COMMENT_ID_MUST_BE_NOT_NULL) Long
  // id,
  //      @Context SecurityContext securityContext) {
  //    Long loggedUserId = getLoggedUserId(securityContext);
  //    articlesService.deleteComment(slug, id, loggedUserId);
  //    return Response.ok().build();
  //  }

  //  @POST
  //  @Path("/{slug}/favorite")
  //  @Secured({Role.ADMIN, Role.USER})
  //  @Produces(MediaType.APPLICATION_JSON)
  //  public Response favoriteArticle(
  //      @PathParam("slug") @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String
  // slug,
  //      @Context SecurityContext securityContext) {
  //    Long loggedUserId = getLoggedUserId(securityContext);
  //    ArticleData articleData = articlesService.favoriteArticle(slug, loggedUserId);
  //    return Response.ok(new ArticleResponse(articleData)).status(Response.Status.OK).build();
  //  }

  //  @DELETE
  //  @Path("/{slug}/favorite")
  //  @Secured({Role.ADMIN, Role.USER})
  //  @Produces(MediaType.APPLICATION_JSON)
  //  public Response unfavoriteArticle(
  //      @PathParam("slug") @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String
  // slug,
  //      @Context SecurityContext securityContext) {
  //    Long loggedUserId = getLoggedUserId(securityContext);
  //    ArticleData articleData = articlesService.unfavoriteArticle(slug, loggedUserId);
  //    return Response.ok(new ArticleResponse(articleData)).status(Response.Status.OK).build();
  //  }

}
