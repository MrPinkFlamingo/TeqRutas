# 🚗 TeqRuta — Sistema de Gestión y Auditoría de Traslados en Terreno

> **Empresa:** TEQMED SpA · **Sede:** Concepción, Chile  
> **Asignatura:** Aplicaciones Móviles (DSY1105) — Evaluación Parcial 2  
> **Stack:** Android Nativo · Kotlin · Jetpack Compose · MVVM · Room 2.7.2 · DataStore · Offline-First  

---

## 📌 1. Descripción del Proyecto

**TeqRuta** es una aplicación móvil Android diseñada para el registro, cálculo de costos y auditoría en tiempo real de los traslados que realizan los técnicos de mantenimiento biomédico de **TEQMED SpA**.

Reemplaza los métodos manuales en papel por un sistema digital automatizado con arquitectura **Offline-First**, permitiendo operar en zonas rurales o subterráneas sin conexión y sincronizar al recuperar señal.

---

## 👥 2. Credenciales de Prueba por Rol

La base de datos local SQLite (Room) se autosemilla en el primer inicio con los siguientes usuarios (todos con contraseña `1234`):

| Rol | Usuario / Correo | Contraseña | Funcionalidades de la Evaluación Parcial 2 |
| :--- | :--- | :--- | :--- |
| **Técnico** | `tecnico@teqmed.cl` o `12345678-9` | `1234` | Iniciar viaje, odómetro inicial, finalizar viaje con odómetro final, peajes, **Cámara nativa** (foto odómetro) y **GPS nativo** (coordenadas). |
| **Jefe de Operaciones** | `jefe@teqmed.cl` o `98765432-1` | `1234` | Bandeja de auditoría, filtro de viajes con desviación (>15%), aprobación o ajuste a km oficial. |

---

## 🏗️ 3. Arquitectura de Software: MVVM Puro (Unidirectional Data Flow)

La aplicación sigue estrictamente el patrón de arquitectura recomendado por Google y las rúbricas de evaluación (**IE 2.2.1** e **IE 2.3.1**):

- **Model:**
  - Entidades Room: `Usuario`, `Vehiculo`, `Destino`, `Traslado`.
  - DAOs reactivos exponiendo `Flow<List<T>>`.
  - `TrasladoRepository`: Centralización de la fórmula oficial de costos:
    $$\text{Combustible} = \left(\frac{\text{Km}}{10}\right) \times 1.280 \quad + \quad \sum \text{Peajes}$$
  - `SessionDataStore`: Persistencia de sesión con Jetpack DataStore Preferences.
- **ViewModel:**
  - `AuthViewModel`: Controla `LoginUiState` (Idle, Loading, Success, Error) y persistencia de sesión.
  - `TrasladoViewModel`: Estados reactivos desacoplados (`formNuevoState`, `formFinalizarState`). Validación centralizada en ViewModel sin acoplamiento a la UI.
- **View:**
  - Interfaz 100% declarativa con **Jetpack Compose** y **Material Design 3**.
  - Sin lógica de negocio ni cálculos en las vistas.
  - Animaciones funcionales con `Modifier.graphicsLayer` y claves estables `key = { it.id }` para eliminar parpadeos.

---

## 📱 4. Recursos Nativos del Dispositivo (IE 2.4.1)

1. **Cámara Fotográfica:** Captura de evidencia y respaldo del odómetro del vehículo en terreno mediante `ActivityResultContracts.TakePicturePreview()`.
2. **Geolocalización / GPS:** Registro de coordenadas de llegada del técnico mediante `LocationManager` y permisos `ACCESS_FINE_LOCATION`.
3. **Conectividad de Red (Offline-First):** Monitoreo en tiempo real del estado de internet mediante `ConnectivityManager.NetworkCallback` y `callbackFlow`, reflejado con badges animados.

---

## 📂 5. Estructura de Paquetes

```text
com.example.teqruta/
├── data/
│   ├── database/       # Room Database, DAOs y semillas de datos
│   ├── datastore/      # SessionDataStore para persistencia de sesión
│   ├── model/          # Data classes y Entities (Usuario, Vehiculo, Destino, Traslado)
│   └── repository/     # TrasladoRepository con lógica financiera
├── ui/
│   ├── navigation/     # NavHost, Rutas y control de acceso por rol
│   ├── screens/        # Pantallas organizadas por módulo (login, tecnico, jefe)
│   ├── theme/          # Paleta institucional TEQMED, tipografías y tema Material 3
│   └── viewmodel/      # AuthViewModel y TrasladoViewModel
└── util/               # NetworkObserver para conectividad en tiempo real
```

---

## 🚀 6. Compilación y Ejecución

### Requisitos:
- Android Studio Iguana / Ladybug o superior.
- JDK 17 o 21 (incluido en Android Studio JBR).
- Android SDK 34 / 35.

### Compilar desde consola:
```powershell
.\gradlew assembleDebug
```
El instalable APK se genera en:
`app/build/outputs/apk/debug/app-debug.apk`
