package com.wish.api.controller;

import com.wish.common.auth.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.wish.api.dto.request.MemberLoginReq;
import com.wish.api.dto.request.MemberSignupReq;
import com.wish.api.dto.request.MemberUpdateReq;
import com.wish.api.dto.response.BaseRes;
import com.wish.api.dto.response.MemberLoginRes;
import com.wish.api.dto.response.MemberRes;
import com.wish.api.service.MemberService;
import com.wish.common.auth.SsafyUserDetails;
import com.wish.common.auth.TestFilter;
import com.wish.common.util.JwtTokenUtil;
import com.wish.db.entity.Member;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;


@Api(value = "유저 관련 API", tags = {"Member"})
@RestController
@RequestMapping("/members")
public class MemberController {
	
	@Autowired
	MemberService memberService;

	// 응답 메세지
	private static final String success = "Success";
	private static final String fail = "fail";
	
	@PostMapping("/signup")
	@ApiOperation(value = "회원 가입", notes = "<strong>아이디, 패스워드, 이름, 이메일</strong>를 입력하여 회원가입한다.") 
    @ApiResponses({
        @ApiResponse(code = 200, message = "성공"),
        @ApiResponse(code = 401, message = "인증 실패"),
        @ApiResponse(code = 404, message = "사용자 없음"),
        @ApiResponse(code = 500, message = "서버 오류")
    })
	public ResponseEntity<? extends BaseRes> signup(
			@RequestBody @ApiParam(value="회원가입 정보", required = true) MemberSignupReq signupInfo) {

		int results_num = memberService.signupMember(signupInfo);

		if(results_num == 0) return ResponseEntity.status(200).body(BaseRes.of(200, "회원가입 성공."));
		else if(results_num == 1) return ResponseEntity.status(401).body(BaseRes.of(401, "이미 가입된 아이디입니다."));
		else return ResponseEntity.status(401).body(BaseRes.of(401, "예상치 못한 결과입니다."));

	}
	
	@PostMapping("/login")
	@ApiOperation(value = "로그인", notes = "<strong>아이디와 패스워드</strong>를 입력하여 로그인한다.") 
    @ApiResponses({
        @ApiResponse(code = 200, message = "성공"),
        @ApiResponse(code = 401, message = "인증 실패"),
        @ApiResponse(code = 404, message = "사용자 없음"),
        @ApiResponse(code = 500, message = "서버 오류")
    })
	public ResponseEntity<? extends BaseRes> login(
			@RequestBody @ApiParam(value="로그인 정보", required = true) MemberLoginReq loginInfo) {
		System.out.println(loginInfo.toString());
		String userId = loginInfo.getId();

		if(memberService.loginMember(loginInfo)) {
			return ResponseEntity.ok(MemberLoginRes.of(200, "Success", JwtTokenUtil.createJwtToken(userId)));
		}

		else return ResponseEntity.status(401).body(BaseRes.of(401, "Fail"));
	}

	@PutMapping
	@ApiOperation(value = "회원수정", notes = "<strong>아이디, 패스워드, 이름, email</strong>를 입력하여 회원정보를 수정한다.")
	@ApiResponses({
			@ApiResponse(code = 200, message = "성공"),
			@ApiResponse(code = 401, message = "인증 실패"),
			@ApiResponse(code = 404, message = "사용자 없음"),
			@ApiResponse(code = 500, message = "서버 오류")
	})
	public ResponseEntity<? extends BaseRes> updateMember(@ApiIgnore Authentication authentication,
			@RequestBody @ApiParam(value="회원수정 정보", required = true) MemberUpdateReq updateInfo) {

		int results_num = memberService.updateMember(updateInfo);

		if(results_num == 0) return ResponseEntity.status(200).body(BaseRes.of(200, "회원수정 성공."));
		else if(results_num == 1) return ResponseEntity.status(401).body(BaseRes.of(401, "등록되지 않은 아이디입니다."));
		else return ResponseEntity.status(401).body(BaseRes.of(401, "예상치 못한 결과입니다."));
	}

	@DeleteMapping
	@ApiOperation(value = "회원탈퇴", notes = "<strong>아이디</strong>를 입력하여 회원탈퇴한다.")
	@ApiResponses({
			@ApiResponse(code = 200, message = "성공"),
			@ApiResponse(code = 401, message = "인증 실패"),
			@ApiResponse(code = 404, message = "사용자 없음"),
			@ApiResponse(code = 500, message = "서버 오류")
	})
	public ResponseEntity<? extends BaseRes> deleteMember(
			@ApiIgnore Authentication authentication) {

		SsafyUserDetails userDetails = (SsafyUserDetails)authentication.getDetails();

		String memberDeleteId = userDetails.getUsername();
		
		int results_num = memberService.deleteMember(memberDeleteId);

		if(results_num==0) return ResponseEntity.status(200).body(BaseRes.of(200, "회원삭제 성공."));
		else if(results_num==1) return ResponseEntity.status(401).body(BaseRes.of(401, "등록되지 않은 아이디입니다."));
		else return ResponseEntity.status(401).body(BaseRes.of(401, "예상치 못한 결과입니다."));
	}

	@GetMapping("/findPW")
	@ApiOperation(value = "비밀번호 찾기", notes = "회원가입할 때 사용했던 <strong>아이디와 이메일</strong>을 입력하여 비밀번호를 찾는다.")
	@ApiResponses({
			@ApiResponse(code = 200, message = "성공"),
			@ApiResponse(code = 401, message = "인증 실패"),
			@ApiResponse(code = 404, message = "사용자 없음"),
			@ApiResponse(code = 500, message = "서버 오류")
	})
	public ResponseEntity<? extends BaseRes> findPassword(
			@ApiParam(value="회원가입했던 아이디", required = true) @RequestParam String memberId,
			@ApiParam(value="회원가입했던 이메일", required = true) @RequestParam String memberEmail) {

				
		int results_num = memberService.findPassword(memberId, memberEmail);

		if(results_num==0) return ResponseEntity.status(200).body(BaseRes.of(200, "가입하신 이메일로 임시 비밀번호가 전송되었습니다."));
		else if(results_num==1) return ResponseEntity.status(401).body(BaseRes.of(401, "아이디나 이메일을 확인해주세요."));
		else return ResponseEntity.status(401).body(BaseRes.of(401, "예상치 못한 결과입니다."));
	}


	@GetMapping("/me")
	@ApiOperation(value = "회원 본인 정보 조회", notes = "로그인한 회원 본인의 정보를 응답한다.") 
    @ApiResponses({
        @ApiResponse(code = 200, message = "성공"),
        @ApiResponse(code = 401, message = "인증 실패"),
        @ApiResponse(code = 404, message = "사용자 없음"),
        @ApiResponse(code = 500, message = "서버 오류")
    })
	public ResponseEntity<MemberRes> getUserInfo(@ApiIgnore Authentication authentication) {

		/**
		 * 요청 헤더 액세스 토큰이 포함된 경우에만 실행되는 인증 처리이후, 리턴되는 인증 정보 객체(authentication) 통해서 요청한 유저 식별.
		 * 액세스 토큰이 없이 요청하는 경우, 403 에러({"error": "Forbidden", "message": "Access Denied"}) 발생.
		 */

		SsafyUserDetails userDetails = (SsafyUserDetails)authentication.getDetails();

		String id = userDetails.getUsername();
		Member member = memberService.getMemberById(id);
		
		return ResponseEntity.status(200).body(MemberRes.of(member));
	}
	
	@GetMapping("/check/id")
	@ApiOperation(value = "회원가입시 아이디 중복 조회", notes = "이미 있는 아이디라면 사용 불가") 
    @ApiResponses({
        @ApiResponse(code = 200, message = "성공"),
        @ApiResponse(code = 401, message = "아이디 중복"),
        @ApiResponse(code = 500, message = "서버 오류")
    })
	public ResponseEntity<BaseRes> checkId(@ApiParam(value="중복검사할 아이디", required = true) String id) {
		// id 가 이상한 문자열이 아닌지 검사->XSS
		if(memberService.checkId(id)) {
			// 중복이 있다 -> 가입 불가
			return ResponseEntity.status(401).body(BaseRes.of(401, fail));
		}else {
			// 중복이 없다 -> 가입 가능
			return ResponseEntity.status(200).body(BaseRes.of(200, success));
		}
	}
	

	@GetMapping("/check/name")
	@ApiOperation(value = "회원가입시 이름 중복 조회", notes = "이미 있는 이름이라면 사용 불가") 
    @ApiResponses({
        @ApiResponse(code = 200, message = "성공"),
        @ApiResponse(code = 401, message = "이름 중복"),
        @ApiResponse(code = 500, message = "서버 오류")
    })
	public ResponseEntity<BaseRes> checkName(@ApiParam(value="중복검사할 이름", required = true)String name) {
		// id 가 이상한 문자열이 아닌지 검사->XSS
		if(memberService.checkName(name)) {
			// 중복이 있다 -> 가입 불가
			return ResponseEntity.status(401).body(BaseRes.of(401, fail));
		}else {
			// 중복이 없다 -> 가입 가능
			return ResponseEntity.status(200).body(BaseRes.of(200, success));
		}
	}
	
}

//http
//.httpBasic().disable() // Http basic Auth  기반으로 로그인 인증창이 뜸.  disable 시에 인증창 뜨지 않음. 
//.csrf().disable()  // rest api이므로 csrf 보안이 필요없으므로 disable처리.
//.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 토큰 기반 인증이므로 세션 사용 하지않음 // jwt token으로 인증하므로 stateless 하도록 처리.
//.and()
////.addFilter(new JwtFilter( authenticationManager(), memberService)) //HTTP 요청에 JWT 토큰 인증 필터를 거치도록 필터를 추가
//.addFilter(new TestFilter(authenticationManager(), memberService))
//.authorizeRequests()
//.antMatchers("/members/me").authenticated() //인증이 필요한 URL
//.anyRequest().permitAll()    // 나머지 모든 요청 허용
//.and().cors();
