# Kiosky 🏪

## Descripción

**Kiosky** es una aplicación innovadora que funciona como un **creador de páginas low-code**, diseñada específicamente para servir como gestor de tiendas. Esta plataforma permite a los usuarios crear y gestionar tiendas virtuales de manera sencilla e intuitiva, sin necesidad de conocimientos técnicos avanzados.

> ⚠️ **Estado del Proyecto**: Esta aplicación se encuentra actualmente en **desarrollo activo**. Algunas funcionalidades pueden estar incompletas o sujetas a cambios.

## Propósito

El objetivo principal de Kiosky es democratizar la creación de tiendas en línea mediante:

- **Enfoque Low-Code**: Permitir a los usuarios crear páginas y tiendas virtuales sin programar
- **Multi-Inquilino**: Soporte para múltiples tiendas independientes en una sola plataforma
- **Facilidad de Uso**: Interface intuitiva para gestión de productos, usuarios y configuraciones
- **Escalabilidad**: Arquitectura robusta basada en Spring Boot para crecimiento futuro

## Tecnologías Utilizadas

- **Backend**: Spring Boot 3.1.5
- **Java**: versión 21
- **Build Tool**: Maven
- **Mapeo de Objetos**: MapStruct 1.5.5
- **Arquitectura**: REST API con patrón MVC

## Estructura del Proyecto

```
src/main/java/com/kiosky/kiosky/
├── controller/          # Controladores REST
├── service/            # Lógica de negocio
├── domain/
│   ├── entity/         # Entidades JPA
│   └── repository/     # Repositorios de datos
├── dto/               # Objetos de transferencia de datos
├── mappers/           # Mappers MapStruct
├── security/          # Configuración de seguridad
├── exception/         # Manejo global de excepciones
└── util/              # Utilidades
```

## Comenzando

### Prerrequisitos

- Java 21 o superior
- Maven 3.6 o superior

### Instalación y Ejecución

1. **Clonar el repositorio**
   ```bash
   git clone <url-del-repositorio>
   cd kiosky
   ```

2. **Compilar el proyecto**
   ```bash
   ./mvnw clean compile
   ```

3. **Ejecutar la aplicación**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Acceder a la aplicación**
   ```
   http://localhost:8080
   ```

## Funcionalidades Principales

### ✅ Implementadas
- Registro y gestión de usuarios
- Registro y gestión de tiendas
- API REST básica
- Arquitectura multi-inquilino

### 🚧 En Desarrollo
- Interface web de usuario
- Sistema de templates para páginas
- Constructor visual low-code
- Gestión de productos y inventario
- Sistema de pagos
- Panel de administración

## API Endpoints

### Usuarios
- `POST /api/users/register` - Registro de nuevos usuarios
- Más endpoints en desarrollo...

### Tiendas
- `POST /api/stores/register` - Registro de nuevas tiendas
- Más endpoints en desarrollo...

## Contribuir

Este proyecto está en desarrollo activo. Las contribuciones son bienvenidas:

1. Fork del proyecto
2. Crear una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit de tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir un Pull Request

## Estado de Desarrollo

- [x] Configuración inicial del proyecto
- [x] Estructura base de la aplicación
- [x] Entidades y repositorios básicos
- [x] Controllers y DTOs iniciales
- [ ] Interface de usuario web
- [ ] Constructor low-code
- [ ] Sistema de autenticación completo
- [ ] Gestión completa de tiendas
- [ ] Sistema de templates
- [ ] Documentación completa de API

## Licencia

Este proyecto está bajo desarrollo. La licencia será definida en versiones futuras.

## Contacto

Para preguntas o sugerencias sobre el proyecto, por favor abre un issue en este repositorio.

---

**Nota**: Este README se actualizará regularmente conforme el proyecto evolucione y nuevas funcionalidades sean implementadas.
