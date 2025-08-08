# LMS Backend - Öğrenme Yönetim Sistemi

**Aday:** Münür Hakan Özcan  
**Teslim Tarihi:** 08.08.2025

Spring Boot ile geliştirilmiş, gelişmiş RBAC (Rol Tabanlı Erişim Kontrolü), kapsamlı denetim günlüğü ve güçlü güvenlik özellikleri içeren kapsamlı bir Öğrenme Yönetim Sistemi backend'i.

## 🚀 Özellikler

### Temel Özellikler
- **Kullanıcı Yönetimi**: Profil türleri (Süper Admin, Öğretmen, Öğrenci) ile tam kullanıcı yaşam döngüsü yönetimi
- **Organizasyon Yönetimi**: Organizasyon ve marka hiyerarşisi ile çok kiracılı destek
- **Sınıf Yönetimi**: Sınıf oluşturma ve atama yönetimi
- **Kurs Yönetimi**: Kurs oluşturma ve sınıflara atama
- **JWT Kimlik Doğrulama**: Yenileme token'ları ile güvenli token tabanlı kimlik doğrulama

### 🔐 Gelişmiş Güvenlik Özellikleri

#### Rol Tabanlı Erişim Kontrolü (RBAC)
- **Esnek İzin Sistemi**: Kaynak-eylem çiftleri ile granüler izinler
- **Rol Yönetimi**: Belirli izinlerle rol oluşturma, güncelleme ve yönetimi
- **Kullanıcı Rol Ataması**: Denetim izi ile kullanıcılara birden fazla rol atama
- **İzin Kontrolü**: API endpoint'leri için gerçek zamanlı izin doğrulama
- **Geriye Uyumluluk**: Gelişmiş RBAC eklerken eski rol sistemini korur

#### Giriş Denemesi Kısıtlama (Hız Sınırlama)
- **E-posta Tabanlı Hız Sınırlama**: E-posta adresi başına yapılandırılabilir limitler
- **IP Tabanlı Hız Sınırlama**: Brute force saldırılarına karşı ek koruma
- **Bucket4j Entegrasyonu**: Verimli token bucket algoritması uygulaması
- **Yapılandırılabilir Pencereler**: Hız sınırlama için ayarlanabilir zaman pencereleri
- **Deneme Takibi**: Tüm giriş denemelerinin kapsamlı günlüğü

#### Denetim Günlüğü Sistemi
- **Kapsamlı Aktivite Takibi**: Detaylı bağlam ile tüm kullanıcı eylemlerini günlüğe kaydetme
- **AOP Tabanlı Günlükleme**: Özel açıklamalarla otomatik metod seviyesi denetim günlüğü
- **Zengin Meta Veri**: IP adresleri, kullanıcı ajanları, zaman damgaları ve işlem detayları
- **Esnek Sorgular**: Kullanıcı, kaynak, eylem veya zaman aralığına göre denetim günlüklerini arama ve filtreleme
- **Performans İzleme**: Metod yürütme sürelerini ve başarı/başarısızlık oranlarını takip etme

### 📊 Test ve Kalite

#### Birim Test Kapsamı (>%80)
- **Kapsamlı Servis Testleri**: İş mantığının tam kapsamı
- **Repository Katmanı Testleri**: TestContainers ile veritabanı işlem testleri
- **Güvenlik Testleri**: Kimlik doğrulama ve yetkilendirme test kapsamı
- **Mock Tabanlı Testler**: Mockito ile verimli birim testleri
- **Entegrasyon Testleri**: Gerçek veritabanı ile uçtan uca testler

### 🛠 Teknik Altyapı

- **Java 21**: Modern dil özellikleri ile en son LTS sürümü
- **Spring Boot 3.5.4**: Spring Security 6 ile en son kararlı sürüm
- **Spring Security**: JWT ve RBAC ile gelişmiş güvenlik
- **Spring Data JPA**: Hibernate ile veritabanı soyutlaması
- **PostgreSQL**: UUID birincil anahtarları ile birincil veritabanı
- **Redis**: Önbellekleme ve oturum yönetimi
- **Docker**: Konteynerleştirme desteği
- **Maven**: Derleme ve bağımlılık yönetimi
- **Lombok**: Azaltılmış boilerplate kodu
- **Swagger/OpenAPI**: API dokümantasyonu

## 📋 Ön Gereksinimler

- Java 21 veya üzeri
- Maven 3.6+
- PostgreSQL 12+
- Redis 6+
- Docker (isteğe bağlı)

## 🚀 Hızlı Başlangıç

### 1. Depoyu Klonlayın
```bash
git clone <repository-url>
cd lms-backend
```

### 2. Veritabanı Kurulumu
```sql
-- Veritabanı oluştur
CREATE DATABASE lms_backend;

-- Kullanıcı oluştur (isteğe bağlı)
CREATE USER lms_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE lms_backend TO lms_user;
```

### 3. Yapılandırma
`src/main/resources/` içinde `application.yml` oluşturun:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/lms_backend
    username: lms_user
    password: your_password
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  
  redis:
    host: localhost
    port: 6379
    password: # gerekirse

  security:
    jwt:
      secret: your-super-secret-jwt-key-here-make-it-long-and-secure
      expiration: 86400000 # 24 saat
      refresh-expiration: 604800000 # 7 gün

lms:
  rate-limit:
    login:
      max-attempts: 5
      window-minutes: 15
    ip:
      max-attempts: 10
      window-minutes: 15

logging:
  level:
    com.lms: DEBUG
    org.springframework.security: DEBUG
```

### 4. Uygulamayı Çalıştırın
```bash
# Maven kullanarak
mvn spring-boot:run

# Docker kullanarak
docker-compose up -d
```

### 5. Uygulamaya Erişin
- **API Temel URL**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Sağlık Kontrolü**: http://localhost:8080/actuator/health

### 6. Örnek Kullanıcılar

#### Süper Admin Kullanıcısı
```json
{
  "email": "admin@lms.com",
  "password": "Admin123!",
  "firstName": "Super",
  "lastName": "Admin",
  "profileId": 0
}
```

#### Öğretmen Kullanıcısı
```json
{
  "email": "teacher@lms.com",
  "password": "Teacher123!",
  "firstName": "John",
  "lastName": "Doe",
  "profileId": 1,
  "organizationId": "your-organization-uuid"
}
```

#### Öğrenci Kullanıcısı
```json
{
  "email": "student@lms.com",
  "password": "Student123!",
  "firstName": "Jane",
  "lastName": "Smith",
  "profileId": 2,
  "organizationId": "your-organization-uuid",
  "classroomId": "your-classroom-uuid"
}
```

## 📚 API Dokümantasyonu

### Kimlik Doğrulama Endpoint'leri
- `POST /api/v1/auth/login` - Kullanıcı girişi
- `POST /api/v1/auth/refresh-token` - JWT token'ını yenileme
- `POST /api/v1/auth/register` - Kullanıcı kaydı (isteğe bağlı)
- `POST /api/v1/auth/logout` - Kullanıcı çıkışı (isteğe bağlı)

### Süper Admin Endpoint'leri
- `POST /api/v1/brands` - Yeni marka oluştur
- `GET /api/v1/brands` - Tüm markaları al
- `PUT /api/v1/brands/{id}` - Marka güncelle
- `DELETE /api/v1/brands/{id}` - Marka sil
- `POST /api/v1/organizations` - Yeni organizasyon oluştur
- `GET /api/v1/organizations` - Tüm organizasyonları al
- `POST /api/v1/users` - Yeni kullanıcı oluştur (profile_id: 1 veya 2)
- `GET /api/v1/users` - Tüm kullanıcıları al
- `POST /api/v1/classrooms` - Yeni sınıf oluştur
- `POST /api/v1/courses` - Yeni kurs oluştur
- `POST /api/v1/courses/assign` - Kursu sınıfa ata
- `POST /api/v1/teachers/assign-classroom` - Öğretmeni sınıfa ata

### Öğretmen Endpoint'leri
- `GET /api/v1/teachers/my-classes` - Öğretmenin atandığı sınıfları al
- `GET /api/v1/teachers/my-students` - Öğretmenin sınıflarındaki öğrencileri al
- `GET /api/v1/teachers/my-courses` - Öğretmenin sınıflarındaki kursları al

### Öğrenci Endpoint'leri
- `GET /api/v1/students/my-courses` - Öğrencinin atandığı kursları al

### RBAC Yönetimi (Gelişmiş Özellikler)
- `GET /api/rbac/roles` - Tüm rolleri al
- `POST /api/rbac/roles` - Yeni rol oluştur
- `PUT /api/rbac/roles/{id}` - Rol güncelle
- `DELETE /api/rbac/roles/{id}` - Rol sil
- `GET /api/rbac/permissions` - Tüm izinleri al
- `POST /api/rbac/permissions` - Yeni izin oluştur
- `POST /api/rbac/users/{userId}/roles` - Kullanıcıya rol ata
- `DELETE /api/rbac/users/{userId}/roles/{roleId}` - Kullanıcıdan rol kaldır

### Denetim Günlükleri
- `GET /api/audit/users/{userId}` - Kullanıcı aktivitesini al
- `GET /api/audit/resources/{type}/{id}` - Kaynak aktivitesini al
- `GET /api/audit/actions/{action}` - Eyleme göre aktiviteyi al
- `GET /api/audit/timerange` - Zaman aralığına göre aktiviteyi al

### Hız Sınırlama
- `GET /api/rate-limit/attempts/email/{email}` - E-posta ile giriş denemelerini al
- `GET /api/rate-limit/attempts/ip/{ip}` - IP ile giriş denemelerini al
- `POST /api/rate-limit/clear` - Hız sınırlama bucket'larını temizle

## 🔧 Geliştirme

### Testleri Çalıştırma
```bash
# Tüm testleri çalıştır
mvn test

# Kapsam raporu ile testleri çalıştır
mvn test jacoco:report

# Belirli test sınıfını çalıştır
mvn test -Dtest=AuditServiceTest

# Entegrasyon testlerini çalıştır
mvn test -Dtest=*IntegrationTest
```

### Kod Kalitesi
```bash
# Kod stilini kontrol et
mvn checkstyle:check

# SonarQube analizi çalıştır
mvn sonar:sonar
```

### Veritabanı Migrasyonları
Uygulama otomatik şema yönetimi için Hibernate'in `ddl-auto: update` özelliğini kullanır. Üretim için, uygun migrasyon yönetimi için Flyway veya Liquibase kullanmayı düşünün.

## 🏗 Proje Yapısı

```
src/
├── main/
│   ├── java/com/lms/
│   │   ├── annotation/          # Özel açıklamalar
│   │   ├── aspect/             # Denetim günlüğü için AOP aspect'leri
│   │   ├── config/             # Yapılandırma sınıfları
│   │   ├── controller/         # REST controller'ları
│   │   ├── dto/               # Veri Transfer Nesneleri
│   │   ├── entity/            # JPA entity'leri
│   │   ├── exception/         # Özel istisnalar
│   │   ├── repository/        # Veri erişim katmanı
│   │   ├── security/          # Güvenlik yapılandırması
│   │   └── service/           # İş mantığı
│   └── resources/
│       ├── application.yml    # Uygulama yapılandırması
│       └── db/               # Veritabanı scriptleri
└── test/
    └── java/com/lms/
        ├── config/            # Test yapılandırması
        └── service/           # Birim testleri
```

## 🔐 Güvenlik Özellikleri

### JWT Kimlik Doğrulama
- Güvenli token tabanlı kimlik doğrulama
- Yapılandırılabilir token süresi
- Yenileme token mekanizması
- Token kara liste desteği

### RBAC Uygulaması
- **İzinler**: Granüler kaynak-eylem izinleri
- **Roller**: İzinlerin koleksiyonları
- **Kullanıcı Rolleri**: Kullanıcılar ve roller arasında çoktan çoğa ilişki
- **Yetkilendirme**: Gerçek zamanlı izin kontrolü

### Hız Sınırlama
- **E-posta Tabanlı**: E-posta başına giriş denemesi limitleri
- **IP Tabanlı**: Saldırılara karşı ek koruma
- **Yapılandırılabilir**: Ayarlanabilir limitler ve zaman pencereleri
- **İzleme**: Kapsamlı deneme takibi

### Denetim Günlüğü
- **Otomatik Günlükleme**: AOP tabanlı metod kesme
- **Zengin Bağlam**: IP, kullanıcı ajanı, zaman damgaları, işlem detayları
- **Performans Takibi**: Metod yürütme süreleri
- **Esnek Sorgular**: Çoklu arama ve filtre seçenekleri

## 📊 İzleme ve Gözlem

### Sağlık Kontrolleri
- Veritabanı bağlantısı
- Redis bağlantısı
- Uygulama durumu

### Metrikler
- İstek/yanıt süreleri
- Hata oranları
- Hız sınırlama istatistikleri
- Denetim günlüğü hacimleri

### Günlükleme
- JSON formatında yapılandırılmış günlükleme
- Tüm işlemler için denetim izi
- Güvenlik olayı günlükleme
- Performans izleme

## 🚀 Dağıtım

### Docker Dağıtımı
```bash
# Görüntü oluştur
docker build -t lms-backend .

# Konteyner çalıştır
docker run -p 8080:8080 lms-backend
```

### Docker Compose
```bash
# Tüm servisleri başlat
docker-compose up -d

# Günlükleri görüntüle
docker-compose logs -f lms-backend
```

### Üretim Hususları
- Harici PostgreSQL ve Redis örnekleri kullanın
- Uygun JWT gizli anahtarlarını yapılandırın
- İzleme ve uyarı sistemleri kurun
- Uygun yedekleme stratejileri uygulayın
- Üretimde HTTPS kullanın
- Hız sınırlamayı uygun şekilde yapılandırın

## 🤝 Katkıda Bulunma

1. Depoyu fork edin
2. Özellik dalı oluşturun (`git checkout -b feature/amazing-feature`)
3. Değişikliklerinizi commit edin (`git commit -m 'Add amazing feature'`)
4. Dalı push edin (`git push origin feature/amazing-feature`)
5. Pull Request açın

## 📝 Lisans

Bu proje MIT Lisansı altında lisanslanmıştır - detaylar için [LICENSE](LICENSE) dosyasına bakın.

## 🆘 Destek

Destek ve sorular için:
- Depoda bir issue oluşturun
- Geliştirme ekibiyle iletişime geçin
- Dokümantasyonu kontrol edin

## 🔄 Sürüm Geçmişi

### v1.0.0
- Temel LMS işlevselliği ile ilk sürüm
- JWT kimlik doğrulama
- Temel kullanıcı ve organizasyon yönetimi
- Sınıf ve kurs yönetimi

### v1.1.0
- Gelişmiş RBAC sistemi
- Kapsamlı denetim günlüğü
- Giriş denemesi kısıtlama
- Gelişmiş güvenlik özellikleri
- %80+ birim test kapsamı
- Postman koleksiyonu

---

**Not**: Bu, kurumsal düzeyde güvenlik özellikleri ile üretim hazırı bir LMS backend'idir. Yapılandırmayı belirli gereksinimlerinize göre gözden geçirdiğinizden ve özelleştirdiğinizden emin olun.
