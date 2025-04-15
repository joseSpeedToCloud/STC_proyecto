package com.gestion_backend.repository_cloud;

import com.gestion_backend.dataModels.UserDataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserDataModel, Long> {

    Optional<UserDataModel> findByUsername(String username);

    long countByRol(UserDataModel.Rol rol);

}
