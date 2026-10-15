# TeqRuta - Sistema de Registro y Gestion de Traslados en Terreno

Empresa: TEQMED SpA - Concepcion, Chile  
Asignatura: Aplicaciones Moviles (DSY1105) - Evaluacion Parcial 2  
Arquitectura: Android Nativo, Kotlin, Jetpack Compose, MVVM, Room 2.7.2, DataStore  

## 1. Descripcion del Proyecto

TeqRuta es una aplicacion movil Android desarrollada para registrar, calcular costos y auditar los traslados que realizan los tecnicos de mantenimiento de equipamiento medico de TEQMED SpA.

La aplicacion opera con persistencia local mediante base de datos SQLite (Room) y DataStore, permitiendo el ingreso y consulta de informacion directamente en el dispositivo.

## 2. Credenciales de Prueba para Evaluacion

La base de datos local SQLite se inicializa con los siguientes usuarios de prueba (contrasena: 1234):

| Rol | Usuario / Correo | Contrasena | Funcionalidades |
| :--- | :--- | :--- | :--- |
| Tecnico | tecnico@teqmed.cl (12345678-9) | 1234 | Iniciar viaje, odometro inicial, finalizar viaje con odometro final, seleccion de peajes, captura de Camara y coordenadas GPS. |
| Jefe de Operaciones | jefe@teqmed.cl (98765432-1) | 1234 | Bandeja de auditoria, filtro de traslados con desviacion, aprobacion y ajuste a kilometraje oficial. |

## 3. Arquitectura de Software: Patron MVVM

El proyecto implementa el patron Model-View-ViewModel con flujo unidireccional de datos (UDF):

- Capa Modelo (data):
  - Entidades Room: Usuario, Vehiculo, Destino, Traslado.
  - Interfaces DAO con retorno reactivo Flow.
  - Repositorio: TrasladoRepository centraliza el calculo de costos de combustible y peajes.
  - DataStore: SessionDataStore para persistencia de la sesion del usuario.
- Capa ViewModel (ui/viewmodel):
  - AuthViewModel: Administra el estado de autenticacion y persistencia de sesion.
  - TrasladoViewModel: Administra los estados de formularios y flujos de datos mediante StateFlow, desacoplando la logica de validacion de la interfaz grafica.
- Capa Vista (ui/screens):
  - Interfaz declarativa en Jetpack Compose y componentes Material Design 3.
  - Formularios con validacion de campos y animaciones funcionales.

## 4. Recursos Nativos del Dispositivo

1. Camara Fotografica: Captura de respaldo del odometro del vehiculo mediante ActivityResultContracts.TakePicturePreview.
2. Geolocalizacion GPS: Obtencion de coordenadas geograficas de llegada mediante LocationManager y permisos en tiempo de ejecucion.

## 5. Estructura de Paquetes

```text
com.example.teqruta/
  data/
    database/       AppDatabase, DAOs y semillas de datos
    datastore/      SessionDataStore para gestion de sesion
    model/          Entidades Usuario, Vehiculo, Destino, Traslado
    repository/     TrasladoRepository
  ui/
    components/     Componentes visuales reutilizables
    navigation/     NavHost y Rutas de navegacion
    screens/        Pantallas login, tecnico, jefe
    theme/          Paleta institucional, tipografia y tema
    viewmodel/      AuthViewModel y TrasladoViewModel
  util/             Observador de red y utilitarios
```

## 6. Compilacion y Ejecucion

Requisitos:
- Android Studio Ladybug / Iguana o superior.
- Java Development Kit (JDK) 17 o superior.
- Android SDK nivel 34 o superior.

Comando de compilacion:
```powershell
.\gradlew assembleDebug
```

El archivo ejecutable APK se genera en:
`app/build/outputs/apk/debug/app-debug.apk`
