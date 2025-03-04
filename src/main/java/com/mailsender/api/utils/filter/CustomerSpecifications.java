package com.mailsender.api.utils.filter;

import com.mailsender.api.enumation.StatusData;
import com.mailsender.api.models.Customer;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class CustomerSpecifications {

    public static Specification<Customer> hasStatus(StatusData status) {
        return (root, query, criteriaBuilder) ->
                status == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Customer> inCompanyIds(List<Long> companyIds) {
        return (root, query, criteriaBuilder) ->
                (companyIds == null || companyIds.isEmpty()) ? criteriaBuilder.conjunction() : root.get("company").get("id").in(companyIds);
    }


    public static Specification<Customer> inBranchIds(List<Long> branchIds) {
        return (root, query, criteriaBuilder) ->
                (branchIds == null || branchIds.isEmpty()) ? criteriaBuilder.conjunction() : root.get("branch").get("id").in(branchIds);
    }

    public static Specification<Customer> hasUsernameOrEmail(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isEmpty()) {
                return criteriaBuilder.conjunction(); // Always true
            }
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("username"), "%" + search + "%"),
                    criteriaBuilder.like(root.get("email"), "%" + search + "%")
            );
        };
    }
}

