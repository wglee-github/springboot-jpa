package jpabook.jpashop.api;

import lombok.Data;

@Data
public class UpdateMemberRequest {
	private String name;
	private String city;
	private String street;
	private String zipcode;
}
