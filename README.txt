스프링 부트를 통해 복잡한 설정이 다 자동화 되었다. persistence.xml도 없고, LocalContainerEntityManagerFactoryBean 도 없다.


■ 외래 키가 있는 곳을 연관관계의 주인으로 정해라.
연관관계의 주인은 단순히 외래 키를 누가 관리하냐의 문제이지 비즈니스상 우위에 있다고 주인으로 정하면 안된다. 
예를 들어서 자동차와 바퀴가 있으면, 일대다 관계에서 항상 다쪽에 외래 키가 있으므로 외래 키가 있는 바퀴를 연관관계의 주인으로 정하면 된다. 
물론 자동차를 연관관계의 주인으로 정하는 것이 불가능한 것은 아니지만, 자동차를 연관관계의 주인으로 정하면 자동차가 관리하지 않는 
바퀴 테이블의 외래 키 값이 업데이트 되므로 관리와 유지보수가 어렵고, 추가적으로 별도의 업데이트 쿼리가 발생하는 성능 문제도 있다. 


■ 엔티티 클래스 개발
예제에서는 설명을 쉽게하기 위해 엔티티 클래스에 Getter, Setter를 모두 열고, 최대한 단순하게 설계했다.
실무에서는 가급적 Getter는 열어두고, Setter는 꼭 필요한 경우에만 사용하는 것을 추천한다.

참고: 이론적으로 Getter, Setter 모두 제공하지 않고, 꼭 필요한 별도의 메서드를 제공하는게 가장
이상적이다. 하지만 실무에서 엔티티의 데이터는 조회할 일이 너무 많으므로, Getter의 경우 모두 열어두는
것이 편리하다. Getter는 아무리 호출해도 호출 하는 것 만으로 어떤 일이 발생하지는 않는다. 하지만
Setter는 문제가 다르다. Setter를 호출하면 데이터가 변한다. Setter를 막 열어두면 가까운 미래에
엔티티에가 도대체 왜 변경되는지 추적하기 점점 힘들어진다. 그래서 엔티티를 변경할 때는 Setter 대신에
변경 지점이 명확하도록 변경을 위한 비즈니스 메서드를 별도로 제공해야 한다.


■ 실무에서는 @ManyToMany 를 사용하지 말자
@ManyToMany 는 편리한 것 같지만, 중간 테이블( CATEGORY_ITEM )에 컬럼을 추가할 수 없고, 세밀하게
쿼리를 실행하기 어렵기 때문에 실무에서 사용하기에는 한계가 있다. 중간 엔티티( CategoryItem 를
만들고 @ManyToOne , @OneToMany 로 매핑해서 사용하자. 정리하면 대다대 매핑을 일대다, 다대일
매핑으로 풀어내서 사용하자.


■ 값 타입은 변경 불가능하게 설계해야 한다.
@Setter 를 제거하고, 생성자에서 값을 모두 초기화해서 변경 불가능한 클래스를 만들자. JPA 스펙상
엔티티나 임베디드 타입( @Embeddable )은 자바 기본 생성자(default constructor)를 public 또는
protected 로 설정해야 한다. public 으로 두는 것 보다는 protected 로 설정하는 것이 그나마 더
안전하다.
JPA가 이런 제약을 두는 이유는 JPA 구현 라이브러리가 객체를 생성할 때 리플랙션 같은 기술을 사용할 수
있도록 지원해야 하기 때문이다.



■ 엔티티 설계시 주의점
엔티티에는 가급적 Setter를 사용하지 말자
Setter가 모두 열려있다. 변경 포인트가 너무 많아서, 유지보수가 어렵다. 나중에 리펙토링으로 Setter 제거
모든 연관관계는 지연로딩으로 설정!
즉시로딩( EAGER )은 예측이 어렵고, 어떤 SQL이 실행될지 추적하기 어렵다. 특히 JPQL을 실행할 때 N+1
문제가 자주 발생한다.
실무에서 모든 연관관계는 지연로딩( LAZY )으로 설정해야 한다.
연관된 엔티티를 함께 DB에서 조회해야 하면, fetch join 또는 엔티티 그래프 기능을 사용한다.
@XToOne(OneToOne, ManyToOne) 관계는 기본이 즉시로딩이므로 직접 지연로딩으로 설정해야
한다.


■ 컬렉션은 필드에서 초기화 하자.
컬렉션은 필드에서 바로 초기화 하는 것이 안전하다.
null 문제에서 안전하다.
하이버네이트는 엔티티를 영속화 할 때, 컬랙션을 감싸서 하이버네이트가 제공하는 내장 컬렉션으로
변경한다. 만약 임의의 메서드에서 컬력션을 잘못 생성하면 하이버네이트 내부
메커니즘에 문제가 발생할 수 있다. 따라서 필드레벨에서 생성하는 것이 가장 안전하고, 코드도 간결하다.

Member member = new Member();
System.out.println(member.getOrders().getClass());
em.persist(member);
System.out.println(member.getOrders().getClass());
//출력 결과
class java.util.ArrayList
class org.hibernate.collection.internal.PersistentBag



■ 테이블, 컬럼명 생성 전략
스프링 부트에서 하이버네이트 기본 매핑 전략을 변경해서 실제 테이블 필드명은 다름
하이버네이트 기존 구현: 엔티티의 필드명을 그대로 테이블의 컬럼명으로 사용
( SpringPhysicalNamingStrategy )


■ 스프링 부트 신규 설정 (엔티티(필드) 테이블(컬럼))
1. 카멜 케이스 언더스코어(memberPoint member_point)
2. .(점) _(언더스코어)
3. 대문자 소문자


적용 2 단계
1. 논리명 생성: 명시적으로 컬럼, 테이블명을 직접 적지 않으면 ImplicitNamingStrategy 사용
spring.jpa.hibernate.naming.implicit-strategy : 테이블이나, 컬럼명을 명시하지 않을 때 논리명 적용,
2. 물리명 적용:
spring.jpa.hibernate.naming.physical-strategy : 모든 논리명에 적용됨, 실제 테이블에 적용
(username usernm 등으로 회사 룰로 바꿀 수 있음)


■ 인젝션으 놀라운 변화

	/**
	 * 
	 	1단계 - 필드 인젝션
	 
	  	단점
	  	- @Autowired 로 선언된 필드 인젝션은 변경이 불가능하다. 
	  	- 그래서 테스트 환경에서 테스트 시 어려움이 있다.
	  	
	 */
//	@Autowired
//	private MemberRepository memberRepository;
	
	
	/**
	 * 
	 	2단계 - 셋터 인젝션
	  	- 스프링이 setter를 통해서 주입(Injection)을 해준다.
	  
  		장점
  		- 테스트 코드 작성 시 아규먼트로 Mock() 을 넣어줄 수 있다.
  
	  	단점 
	  	- 누군가가 런타임 시점에 setter 메소드 자체를 변경 할 수 있다.
	  	- 실제 스프링 런타임 이 후 인젝션한 필드를 변경 할 이슈가 없다. 그렇기 때문에 변경이 가능하게 만들 필요가 없다.
	 * 
	 */
//	private MemberRepository memberRepository;
//	
//	@Autowired
//	public void setMemberRepository(MemberRepository memberRepository) {
//		this.memberRepository = memberRepository;
//	}

	
	/**
	 * 
		3단계 - 생성자 인젝션 1
		- 스프링이 생성자를 통해 필드에 주입(Injection)해 준다.
		 
		장점
		- 필드에 주입한 이후 변경이 불가능 하다.
		- MemberService memberService = new MemberService(); 생성 시 컴파일 시점에 아규먼트가 필요하다고 에러가 발생해서 아큐먼트 주입을 놓치지 않을 수 있다.
	  
	 */
//	private MemberRepository memberRepository;
//	
//	@Autowired
//	public MemberService(MemberRepository memberRepository) {
//		this.memberRepository = memberRepository;
//	}

	
	/**
	 * 4단계 - 생성자 인젝션  2
	 * 
	 * 1. 생성자가 하나만 있는 경우에는 @Autowired 가 없어도 스프링이 알아서 인젝션을 해준다.
	 * 2. 필드 선언 시 final을 지정해 주자
	 * 
	 */
//	private final MemberRepository memberRepository;
//	
//	public MemberService(MemberRepository memberRepository) {
//		this.memberRepository = memberRepository;
//	}
	
	/**
	 * 5단계 - 생성자 인젝션  3
	 * - 품북에서 지원함.
	 * Class에 @AllArgsConstructor 를 걸어주면 선언된 필드들 모두 생성자 인젝션을 만들어 준다. 즉, 생성자 부분 생략 가능하다.
	 * 
	 */
//	private final MemberRepository memberRepository;
	
	
	
	/**
	 * 6단계(최종) - 생성자 인젝션  4
	 * - 품북에서 지원함.
	 * Class에 @RequiredArgsConstructor 를 걸어주면 final로 선언된 필드만 생성자 인젝션을 만들어 준다.
	 * 
	 */
	private final MemberRepository memberRepository;

■ 테스트 설정 파일
테스트에서 스프링을 실행하면 src/test/resource 에 있는 설정 파일을 읽는다.
(만약 이 위치에 없으면 src/resources/application.yml 을 읽는다.)
스프링 부트는 datasource 설정이 없으면, 기본적을 메모리 DB를 사용하고, driver-class도 현재 등록된 라이브러리를 보고 찾아준다. 
따라서 데이터소스나,JPA 관련된 별도의 추가 설정을 하지 않아도 된다.(application.yml 파일에 DB 설정이 아예 없어도 됨)
추가로 ddl-auto 도 create-drop 모드로 동작한다.

	
■ 테스트 applicaiton.yml 추가하는 방법 (Spring Boot 버전 2.3.x 부터 src/test/resources 제거됨 )
1. 클래스 경로인 src/test 밑에 만들어야 한다. 
2. 만들 때 마우스 우클릭 > New > source folder 를 선택한다.
2. src/test/resource/config  까지 만든다.
3. confige 아래에 applicaiton.yml 추가
4. Gradle Refresh Project 해줘야한다.
** 추가 후 여전히 main의 application.yml를 적용하면 삭제 했다가 다시 넣었다가 프로젝트 클린도 했다가 하면 된다.
