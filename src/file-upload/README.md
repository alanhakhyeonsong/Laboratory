# 스프링 파일 업로드 with Vue.js
Spring File Upload에 관해 학습  
이에 더해, REST API로 변환 후 Vue.js에서 업로드 테스트

## Used Stack
- Backend: Spring Boot, Spring MVC
- Frontend: Vue.js, Vue CLI, Axios

## 구현 내용
Spring Boot Backend는 `localhost:8080`에서 실행중이다.  
김영한님의 강의 내용은 Thymeleaf 뷰 템플릿을 사용하여 서버 사이드 렌더링으로 구현한 간단한 예제이다.

만약, Frontend와 Backend가 분리된 구조라면 단순 업로드 API는 어떻게 구현하는지 공부해보고 싶었다. (UI는 논외로 한다.)

먼저, Vue.js로 구현한 Frontend는 `npm run serve -- --port 3000`로 실행시킨다.

![](https://velog.velcdn.com/images/songs4805/post/524e0a40-4a54-4a0f-9f2f-89a7677463dc/image.png)

### Codes
먼저, CORS에 걸리지 않도록 다음과 같이 설정 Bean을 등록해준다.
```java
@Bean
public WebMvcConfigurer corsConfigurer() {
	return new WebMvcConfigurer() {
		@Override
		public void addCorsMappings(CorsRegistry registry) {
			registry.addMapping("/**").allowedOrigins("http://localhost:3000");
		}
	};
}
```

`RestImageUploadController.java`
```java
package ramos.upload.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/rest")
@Slf4j
public class RestImageUploadController {

    @Value("${file.dir}")
    private String fileDir;

    @PostMapping("/upload")
    public ResponseEntity<Object> upload(@RequestParam MultipartFile uploadFile) throws IOException {
        log.info("multipartFile={}", uploadFile);

        if (!uploadFile.isEmpty()) {
            String fullPath = fileDir + uploadFile.getOriginalFilename();
            log.info("파일 저장 fullPath={}", fullPath);
            uploadFile.transferTo(new File(fullPath));
        }

        return new ResponseEntity<>(uploadFile.getOriginalFilename(), HttpStatus.OK);
    }
}
```

`UploadForm.vue`
```javascript
<template>
  <form @submit.prevent="submit">
    <div>
      <label for="uploadfile">Upload</label>
      <input name="uploadfile" id="uploadfile" type="file" ref="uploadfile" />
    </div>
    <button type="submit">submit</button>
  </form>
  <p>{{ resultMessage }}</p>
</template>

<script>
import { upload } from '@/api/index';

export default {
  name: "UploadForm",
  data() {
    return {
      uploadFile: '',
      resultMessage: '',
    };
  },
  methods: {
    async submit() {
      const formData = new FormData();
      formData.append('uploadFile', this.$refs['uploadfile'].files[0]);
      try {
        await upload(formData);
        this.resultMessage = "success";
      } catch (error) {
        console.log(error);
        this.resultMessage = error;
      }
    }
  },
}
</script>

<style scoped>

</style>
```

`index.js`
```javascript
// api 호출
import axios from "axios";

const instance = axios.create({
    baseURL: 'http://localhost:8080',
});

function upload(data) {
    return instance.post('/rest/upload', data, {
        headers: {
            'Content-Type': 'multipart/form-data',
            'Access-Control-Allow-Origin': '*',
        }
    });
}

export { upload }
```

### 실행 결과
![](https://velog.velcdn.com/images/songs4805/post/6eb161ef-c378-48ae-9eab-403831140102/image.png)

![](https://velog.velcdn.com/images/songs4805/post/f9bd6a70-beb3-457d-a1b6-410756af06c6/image.png)
이미지 파일을 위와 같이 선택하여 submit을 클릭한다.

![](https://velog.velcdn.com/images/songs4805/post/43102e60-4816-4a93-a89c-f7051abf47b6/image.png)
요청 결과 정상 응답이 반환되었다.

![](https://velog.velcdn.com/images/songs4805/post/1321d2c6-849a-49ec-9218-8c6f866472e4/image.png)
그 결과, 내가 지정한 업로드 경로(`/Users/hakhyeonsong/study/filetest/`)로 해당 이미지 파일이 정상적으로 업로드 되었다.

![](https://velog.velcdn.com/images/songs4805/post/f25a1f21-f99a-4a75-83df-fbc378dec72d/image.png)
로그는 다음과 같이 정상적으로 출력되었다.

## References
- [김영한님의 스프링 MVC 2편 - 백엔드 웹 개발 활용 기술](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-mvc-2)