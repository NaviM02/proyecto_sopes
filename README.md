# REDXela

Simulador de escritorio para un centro de distribución de comercio electrónico. El proyecto modela pedidos, inventario, recursos compartidos y coordinación entre recepción e inventario con Java 17, hilos y Swing.

## Requisitos

- JDK 17 o superior.
- Maven 3.8 o superior.
- IntelliJ IDEA (opcional, para abrir y ejecutar el proyecto).

## Ejecutar

### Desde IntelliJ IDEA

1. Abre la carpeta del proyecto o selecciona `pom.xml` desde **File → Open**.
2. Configura un JDK 17 en **Project Structure → Project SDK** y carga el proyecto Maven.
3. Ejecuta la clase `com.navi.redxela.Main`.

El código está bajo el paquete `com.navi.redxela`, en la estructura estándar de Maven.

### Desde una terminal

```bash
mvn clean package
java -jar target/redxela-1.0.0.jar
```

La aplicación requiere un entorno de escritorio con soporte para Swing.

## Capacidad del almacén

La capacidad inicial es de 500 unidades y se puede cambiar al iniciar la aplicación:

```bash
java -Dredxela.capacidad=800 -jar target/redxela-1.0.0.jar
```

También puedes agregar `-Dredxela.capacidad=800` en las opciones de VM de la configuración de ejecución de IntelliJ. La capacidad mínima aceptada es de 20 unidades para conservar las ubicaciones físicas del almacén.

## Qué incluye

- Registro de cuentas con cinco niveles de servicio.
- Pedidos manuales con uno o varios productos y clasificación por tipo de mercancía.
- Generador continuo de pedidos de prueba.
- Tres políticas para ordenar la cola de espera.
- Semáforos para administrar las estaciones, personal, montacargas, carga y escáner.
- Recepción asíncrona mediante una cola acotada y asignación de ubicaciones físicas.
- Reserva, despacho y devolución de existencias.
- Laboratorio interactivo que provoca y permite resolver un interbloqueo real entre hilos.
- Estadísticas de espera, procesamiento y ocupación de recursos.

## Documentación

- [Manual de usuario](Manual%20de%20Usuario.md)
- [Manual técnico](Manual%20T%C3%A9cnico.md)
- [Planteamiento original](Proyecto%20Final.md)

Los datos se conservan en memoria durante la ejecución. Al cerrar y volver a abrir la aplicación, la simulación comienza con las cuentas y existencias de ejemplo.
