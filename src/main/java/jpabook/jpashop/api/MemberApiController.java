package jpabook.jpashop.api;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

	private final MemberService memberService;
	
	/**
	 * 응답이 엔티티 자체인 경우
	 	· 문제점
			· 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.( 회원 조회 API 종류가 다양할 경우 특정 컬럼을 제외하고 보내야 하는 경우 등에 따라 @JsonIgnore 이런 설저을 해줘야 함)
		 	· 기본적으로 엔티티의 모든 값이 노출된다.
			· 응답 스펙을 맞추기 위해 로직이 추가된다. (@JsonIgnore, 별도의 뷰 로직 등등)
			· 실무에서는 같은 엔티티에 대해 API가 용도에 따라 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한 프레젠테이션 응답 로직을 담기는 어렵다.
			· 엔티티가 변경되면 API 스펙이 변한다.
			
			· 결과 포맷의 문제
			- 응답이 배열([])로 됨. 오브젝트({})로 되어야 응답 필드를 추가하는등의 유연성이 확보됨.
			나쁜예)
			[
			    {
			        "id": 1,
			        "name": "회원1",
			        "address": {
			            "city": "송도",
			            "street": "연수",
			            "zipcode": "12345"
			        },
			        "orders": []
			    }
			]
 
	 */
	@GetMapping("/api/v1/members")
	public List<Member> membersV1(){
		return memberService.findMembers();
	}
	
	
	/**
	 * 응답은 별도의 DTO를 사용하여 반환한다. 
	 * 
	 	좋은 예)
	 	{
		    "data": [
		        {
		            "name": "회원1",
		            "city": "송도",
		            "street": "연수",
		            "zipcode": "12345"
		        }
		    ]
		}
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@GetMapping("/api/v2/members")
	public Result membersV2(){
		List<Member> members = memberService.findMembers();
		List<MemberDTO> memberDTOs = members.stream().map(m -> 
			new MemberDTO(m.getName(), m.getAddress().getCity(), m.getAddress().getStreet(), m.getAddress().getZipcode()))
			.collect(Collectors.toList());
		
		return new Result(memberDTOs.size(), memberDTOs);
	}
	
	@Data
	@AllArgsConstructor
	static class Result<T> {
		private int count;
		private T data;
	}
	
	@Data
	@AllArgsConstructor
	static class MemberDTO {
		private String name;
		private String city;
		private String street;
		private String zipcode;
	}
	
	/**
	 * @RequestBody
	 * - json request의 body를 Member에 맵핑해 준다.
	 * 
	 * 값타입 API 요청 포맷
	 	{
    		"name" : "회원7",
    		"address" : {"city" : "송도", "street" : "연수", "zipcode":"12345"} 
		}
		
		엔티티를 직접 맵핑 하는건 좋지 않다.
		엔티티 변경시 API 스펙이 변경되게 되어 문제가 발생한다.
		따라서 API 요청 스펙에 맞춘 별도 DTO를 만들어서 해야한다. 
		@RequestBody @Valid Member member X
	 * 
	 */
	@PostMapping("/api/v1/members")
	public CreateMemeberResponse saveMemberV1(@RequestBody @Valid Member member) {
		Long id = memberService.join(member);
		return new CreateMemeberResponse(id);
	}
	
	/**
	 * API 요청 스펙에 맞춘 별도 DTO 생성.
	 * API 요청 맵핑 class를 만들었기 때문에 요청 시 값 타입 포맷 상관 없음.
	 	{
		    "name" : "회원9",
		    "city" : "송도", 
		    "street" : "연수", 
		    "zipcode":"12345"
		} 
	 */
	@PostMapping("/api/v2/members")
	public CreateMemeberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest memberReq) {
		Member member = new Member();
		member.setName(memberReq.getName());
		member.setAddress(new Address(memberReq.getCity(), memberReq.getStreet(), memberReq.getZipcode()));
		
		Long id = memberService.join(member);
		return new CreateMemeberResponse(id);
	}
	
	@PutMapping("/api/v2/members/{id}")
	public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id, @RequestBody @Valid UpdateMemberRequest memberReq){
		memberService.updateMember(id, memberReq);
		Member findMember = memberService.findOne(id);
		return new UpdateMemberResponse(findMember.getId(), findMember.getName());
	}
	
	/**
	 *	 
	 * Update API response class
	 */
	@Data
	@AllArgsConstructor
	public class UpdateMemberResponse {
		private Long id;
		private String name;
	}
	
	/**
	 *	 
	 * Create API request mapping class
	 */
	@Data
	static class CreateMemberRequest {
		@NotEmpty(message = "회원이름은 필수값 입니다.")
		private String name;
		private String city;
		private String street;
		private String zipcode;
	}
	
	/**
	 * 
	 * Create API response class
	 */
	@Data
	static class CreateMemeberResponse{
		private Long id;

		public CreateMemeberResponse(Long id) {
			this.id = id;
		}
	}
}
