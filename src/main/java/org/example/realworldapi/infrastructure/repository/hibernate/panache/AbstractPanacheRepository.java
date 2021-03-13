package org.example.realworldapi.infrastructure.repository.hibernate.panache;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.ArticleEntity;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.TagEntity;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.UserEntity;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AbstractPanacheRepository<ENTITY, ID> implements PanacheRepositoryBase<ENTITY, ID> {

  protected UserEntity findUserEntityById(UUID id) {
    return getEntityManager().find(UserEntity.class, id);
  }

  protected TagEntity findTagEntityById(UUID id) {
    return getEntityManager().find(TagEntity.class, id);
  }

  protected ArticleEntity findArticleEntityById(UUID id) {
    return getEntityManager().find(ArticleEntity.class, id);
  }

  protected boolean isNotEmpty(List<?> list) {
    return list != null && !list.isEmpty();
  }

  protected List<String> toUpperCase(List<String> subjectList) {
    return subjectList.stream().map(String::toUpperCase).collect(Collectors.toList());
  }
}
