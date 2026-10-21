# Manual de usuario — REDXela

## 1. Propósito

REDXela permite observar cómo se reciben pedidos, se asignan recursos y se despacha mercancía en un centro de distribución. La aplicación es una simulación de escritorio: sus datos viven en memoria mientras está abierta.

## 2. Requisitos e inicio

- JDK 17 o superior.
- IntelliJ IDEA o Maven.
- Un entorno de escritorio que pueda mostrar ventanas Swing.

En IntelliJ, abre el proyecto por medio de `pom.xml`, carga Maven y ejecuta `com.navi.redxela.Main`. También puedes construir y ejecutar desde una terminal:

```bash
mvn clean package
java -jar target/redxela-1.0.0.jar
```

Al iniciar, REDXela carga cinco cuentas de ejemplo y existencias iniciales para los ocho productos del catálogo. El almacén tiene una capacidad predeterminada de 500 unidades.

## 3. Navegación principal

La barra lateral contiene cuatro vistas:

| Vista | Para qué sirve |
|---|---|
| **Resumen** | Consultar estadísticas, pedidos activos, cola de espera y recursos. |
| **Pedidos** | Registrar cuentas y pedidos, cambiar la política y controlar el generador. |
| **Inventario** | Registrar recepción de mercancía y revisar existencias y ubicaciones. |
| **Conflicto** | Provocar un interbloqueo de demostración y escoger la operación que continúa. |

La aplicación refresca la información mientras está abierta. El reloj y el indicador de la barra superior muestran el estado general.

## 4. Resumen de operaciones

La pantalla de resumen muestra cuatro indicadores:

- **Pedidos completados:** cantidad despachada desde que se inició la aplicación.
- **Espera promedio:** promedio del tiempo entre el registro y el comienzo del proceso para pedidos completados.
- **Proceso promedio:** promedio del tiempo de procesamiento de pedidos completados.
- **Ocupación de recursos:** proporción de unidades de recurso que están ocupadas en ese momento.

Las tablas inferiores separan los pedidos **en proceso** de los que están **en espera**. Para cada operación activa se muestra progreso y tiempo restante estimado. La cola se ordena con la política seleccionada en la vista Pedidos.

En **Recursos compartidos** se ve la capacidad total, las unidades libres y los pedidos u operaciones que tienen recursos ocupados.

## 5. Cuentas y niveles de servicio

Cada cuenta usa un nivel de servicio, que se aplica a sus pedidos:

| Nivel | Servicio | Objetivo de espera |
|---:|---|---|
| 1 | Exprés | Casi instantáneo |
| 2 | Prioritario | Máximo 10 minutos |
| 3 | Estándar | Máximo 30 minutos |
| 4 | Programado | Máximo 60 minutos |
| 5 | Económico | Máximo 120 minutos |

Para registrar una cuenta:

1. Abre **Pedidos**.
2. Selecciona **Nueva cuenta**.
3. Escribe el nombre y escoge el nivel de servicio.
4. Confirma el registro.

Las cuentas de ejemplo son útiles para probar todos los niveles sin crear cuentas adicionales.

## 6. Registrar pedidos

1. En **Pedidos**, selecciona **+ Nuevo pedido**.
2. Escoge la cuenta del cliente. El nivel de servicio se toma de esa cuenta.
3. Pulsa **Agregar artículo**.
4. Selecciona un producto del catálogo e indica las unidades.
5. Repite el paso anterior para agregar otros productos; puedes quitar la línea seleccionada si te equivocaste.
6. Pulsa **Crear pedido**.

Cada artículo identifica el tipo de mercancía del producto. Un pedido con varios tipos requiere la unión de los recursos asociados a esos tipos. La cantidad total de unidades sirve como estimación de trabajo: los pedidos grandes tardan más.

Los estados que puede mostrar un pedido son:

- **En espera:** todavía no comenzó el procesamiento.
- **Esperando inventario:** no hay unidades disponibles de uno o más productos.
- **Esperando recursos:** hay existencias, pero el conjunto de recursos requerido no está libre.
- **En proceso:** el pedido reservó inventario y recursos; el progreso se actualiza durante la simulación.
- **Completado:** el inventario se despachó y los recursos fueron liberados.

Un pedido sin existencias no se elimina ni se pierde: vuelve a evaluarse automáticamente cuando hay nuevas recepciones.

## 7. Comparar políticas de atención

En **Pedidos**, selecciona una política. La descripción junto al selector resume su criterio:

1. **Prioridad estricta:** atiende primero el nivel de servicio más alto; los pedidos del mismo nivel se ordenan por antigüedad.
2. **Prioridad con antigüedad:** el tiempo esperando mejora poco a poco la posición. Cada cinco minutos de espera compensan aproximadamente un nivel de prioridad.
3. **Menos unidades primero:** empieza por los pedidos cortos para liberar recursos antes; el nivel de servicio y la antigüedad desempatan.

Cambiar de política vuelve a ordenar la cola. No interrumpe los pedidos que ya están en proceso.

## 8. Generador automático

En **Pedidos**, pulsa **Iniciar generador** para crear pedidos de prueba continuamente. El generador usa las cuentas y productos existentes, mezcla los niveles de servicio y agrega una llegada aproximadamente cada cuatro segundos.

Pulsa **Detener generador** para pausarlo. Los pedidos que ya se generaron permanecen en el sistema y continúan su ciclo normal. El generador no añade existencias; si consume el stock disponible, otros pedidos esperarán hasta que se registre una recepción.

## 9. Recibir mercancía y consultar el almacén

1. Abre **Inventario y recepción**.
2. Selecciona un producto del catálogo.
3. Indica la cantidad de unidades recibidas.
4. Pulsa **Enviar recepción**.

Recepción coloca una solicitud en una cola acotada. Inventario la procesa en su propio hilo, actualiza las existencias y distribuye las unidades en ubicaciones físicas. En la tabla **Cola de recepción** puedes ver si cada solicitud está en cola, procesándose, aplicada o rechazada.

La tabla **Existencias por producto** separa:

- **Disponibles:** unidades que un pedido todavía puede reservar.
- **Reservadas:** unidades comprometidas con pedidos que están en proceso.
- **Ubicaciones físicas:** pasillo y nivel donde se almacena cada parte del producto.

El mismo lote puede aparecer repartido entre varias ubicaciones. Si no hay espacio suficiente, la solicitud se rechaza con el número de unidades libres; el usuario puede reducir la cantidad e intentar otra recepción. Si la cola está llena, la solicitud no se acepta y la pantalla permite volver a enviarla más tarde.

## 10. Provocar y resolver un conflicto

Abre **Conflicto de recursos** y pulsa **Provocar conflicto**. REDXela crea dos hilos que solicitan los mismos recursos en orden inverso:

| Operación | Recurso que retiene | Recurso que espera |
|---|---|---|
| A | Escáner | Estaciones de calidad |
| B | Las cuatro estaciones de calidad | Escáner |

Como cada operación conserva un recurso que necesita la otra, ambas quedan esperando. El escenario usa los semáforos reales del gestor de recursos. Si el escáner o alguna estación de calidad ya está ocupada, el escenario no se inicia; espera a que los pedidos liberen esos recursos y vuelve a probar.

Cuando la pantalla indique **Bloqueo detectado**, pulsa **Continuar con A** o **Continuar con B**. El hilo elegido adquiere el recurso que esperaba y termina. El hilo descartado se interrumpe de forma controlada, libera lo que retenía y queda marcado para regresar a la lista de espera. El resultado aparece en las tarjetas de ambas operaciones.

El ejemplo termina automáticamente y libera los recursos para que el resto de la simulación continúe.

## 11. Tipos de mercancía y recursos

| Tipo | Recursos requeridos |
|---|---|
| Frágil | Calidad y empaque |
| Pesada | Montacargas y área de carga |
| Refrigerada | Personal de bodega y montacargas |
| Electrónica | Escáner y calidad |
| Peligrosa | Escáner, calidad y personal de bodega |

Electrónica y peligrosa comparten el escáner y calidad, por lo que pueden competir por ambos recursos.

## 12. Almacén y datos

El almacén tiene 20 ubicaciones y 500 unidades de capacidad por defecto. Para cambiar la capacidad, configura la propiedad de Java antes de iniciar:

```bash
java -Dredxela.capacidad=800 -jar target/redxela-1.0.0.jar
```

En IntelliJ, coloca `-Dredxela.capacidad=800` en **Run → Edit Configurations → VM options**. La capacidad mínima es de 20 unidades.

Las cuentas, pedidos, recepciones e inventario se guardan en memoria. Al cerrar la aplicación, los datos de esa sesión desaparecen; al volver a abrirla se cargan los datos iniciales de ejemplo.

## 13. Solución de problemas

| Situación | Qué hacer |
|---|---|
| Un pedido queda esperando inventario | Registra una recepción de cada producto faltante. |
| Un pedido espera recursos | Deja que finalicen otros pedidos; el planificador volverá a intentarlo. |
| Una recepción es rechazada por capacidad | Reduce la cantidad o despacha pedidos que ocupen espacio. |
| No inicia el conflicto | Espera a que el escáner y todas las estaciones de calidad estén libres. |
| No abre la ventana | Confirma que se ejecuta con JDK 17 y en un entorno con escritorio, no en modo headless. |
| Maven no compila | Comprueba el JDK seleccionado en IntelliJ y vuelve a cargar el proyecto desde `pom.xml`. |
