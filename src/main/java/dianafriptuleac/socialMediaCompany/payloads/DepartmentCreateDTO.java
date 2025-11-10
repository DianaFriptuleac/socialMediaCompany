package dianafriptuleac.socialMediaCompany.payloads;

import dianafriptuleac.socialMediaCompany.enums.DepartmentType;

public record DepartmentCreateDTO(DepartmentType departmentType, String description) {
}
