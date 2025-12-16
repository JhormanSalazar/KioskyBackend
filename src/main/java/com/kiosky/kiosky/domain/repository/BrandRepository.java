package com.kiosky.kiosky.domain.repository;

import com.kiosky.kiosky.domain.entity.AppUser;
import com.kiosky.kiosky.domain.entity.Brand;
import com.kiosky.kiosky.domain.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long>{

}
