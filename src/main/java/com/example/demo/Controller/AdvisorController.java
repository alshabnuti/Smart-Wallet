package com.example.demo.Controller;

import com.example.demo.model.User;
import com.example.demo.Repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/advisor")
@CrossOrigin(origins = "http://localhost:4200") // للسماح لـ Angular بالاتصال
public class AdvisorController {

    private final UserRepository userRepository;

    // جلب الإعدادات التي أضفناها في ملف application.properties تلقائياً
    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    public AdvisorController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/evaluate/{userId}")
    // أضفنا استلام اللغة (lang) من الرابط، وإذا لم يرسلها التطبيق نعتبرها 'ar'
    // كافتراضي
    public ResponseEntity<?> getFinancialAdvice(@PathVariable Long userId,
            @RequestParam(defaultValue = "ar") String lang) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("المستخدم غير موجود!");
        }

        // 👈 هذا هو السحر: شرط يحدد لغة الرد بناءً على لغة التطبيق
        String languageInstruction = lang.equalsIgnoreCase("en")
                ? "IMPORTANT: You MUST answer entirely in English."
                : "هام جداً: تحدث معه مباشرة باللغة العربية.";

        // صياغة السؤال مع دمج أمر اللغة
        String prompt = String.format(
                "أنت مستشار مالي ذكي، ودود، ومرح ومحفز. قم بتحليل الوضع المالي للمستخدم التالي وقدم له تقييماً سريعاً ونصيحة ذهبية وتوقعاً ذكياً متى سيصل لهدفه المالي.\n"
                        + "%s\n" // 👈 وضعنا أمر اللغة هنا ليقرأه Gemini أولاً
                        + "بأسلوب مشجع ومختصر جداً (على شكل 3 نقاط أو أسطر قصيرة كحد أقصى):\n\n"
                        + "- اسم المستخدم: %s\n"
                        + "- الراتب الشهري: %.2f\n"
                        + "- الهدف المالي الحالي: %s (المبلغ المطلوب: %.2f)\n"
                        + "- المبلغ المتوفر حالياً في الحصالة التراكمية للهدف: %.2f\n"
                        + "ملاحظة: إذا كان راتبه كافياً ومصروفاته متزنة، شجعه وأخبره بموعد تقريبي ذكي للوصول للهدف. وإذا كان يواجه عجزاً، اقترح عليه تقليل المصاريف بلطف.",
                languageInstruction, // 👈 تمرير أمر اللغة
                user.getName(),
                user.getSalary(),
                user.getGoalName() != null ? user.getGoalName() : "لم يحدد هدفاً بعد",
                user.getGoalAmount() != null ? user.getGoalAmount() : 0.0,
                user.getSavedBalance() != null ? user.getSavedBalance() : 0.0);

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", prompt);

            Map<String, Object> partContainer = new HashMap<>();
            partContainer.put("parts", Collections.singletonList(textPart));

            requestBody.put("contents", Collections.singletonList(partContainer));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String fullUrl = apiUrl + apiKey;
            ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.POST, entity, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            String advice = root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            Map<String, String> result = new HashMap<>();
            result.put("advice", advice);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("حدث خطأ أثناء الاتصال بالمستشار المالي الذكي: " + e.getMessage());
        }
    }
}