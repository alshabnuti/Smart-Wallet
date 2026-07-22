# مرحلة البناء باستخدام Maven وجافا 17
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# نسخ ملفات المشروع الرئيسية
COPY pom.xml .
COPY src ./src

# بناء المشروع وتجاهل الاختبارات لتسريع العملية
RUN mvn clean package -DskipTests

# مرحلة التشغيل النهائية (نسخة جافا خفيفة الوزن)
FROM eclipse-temurin:17-jre
WORKDIR /app

# نسخ الملف التنفيذي النهائي من مرحلة البناء
COPY --from=build /app/target/*.jar app.jar

# تحديد المنفذ الذي سيعمل عليه السيرفر
EXPOSE 8080

# أمر التشغيل الأساسي
ENTRYPOINT ["java", "-jar", "app.jar"]