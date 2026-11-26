package com.estructura.datos.cletter.datos;

import com.estructura.datos.cletter.modelo.PisoInfo;

/**
 * Catálogo completo de los 20 pisos (200 niveles) dedicados a la historia
 * de los Videojuegos y las Ciencias de la Computación.
 *
 * Cada piso apunta automáticamente a su portada en:
 * app/src/main/res/drawable-nodpi/floor_01.webp ... floor_20.webp
 */
public final class CatalogoPisos {

    public static final int TOTAL_PISOS = 20;
    public static final int NIVELES_POR_PISO = 10;
    public static final int TOTAL_NIVELES = TOTAL_PISOS * NIVELES_POR_PISO;

    private static final PisoInfo[] PISOS = new PisoInfo[] {
            new PisoInfo(
                    1,
                    "El Primer Algoritmo", "The First Algorithm",
                    "Ada Lovelace", "Ada Lovelace",
                    PisoInfo.AREA_CS, "1843",
                    "En 1843, Ada Lovelace escribió las célebres notas sobre la Máquina Analítica de Charles Babbage. En la Nota G diseñó el primer algoritmo publicado pensado para ser ejecutado por una máquina, imaginando ordenadores capaces de crear música y arte más allá del cálculo numérico.",
                    "In 1843, Ada Lovelace wrote her celebrated notes on Charles Babbage's Analytical Engine. In Note G she published the first algorithm intended for machine execution, envisioning computers capable of composing music and art beyond pure arithmetic.",
                    "floor_01", 0xFF6B1D2E, 0xFFE68A6B,
                    new String[]{"PASO", "NOTA", "DATO", "CICLO", "LOGICA", "ORDEN", "TABLA", "MOTOR"},
                    new String[]{"LOOP", "NOTE", "DATA", "CODE", "LOGIC", "STEP", "TABLE", "ENGINE"}
            ),
            new PisoInfo(
                    2,
                    "La Máquina de Turing", "The Turing Machine",
                    "Alan Turing", "Alan Turing",
                    PisoInfo.AREA_CS, "1936",
                    "Alan Turing formalizó el concepto de algoritmo y computabilidad con una cinta infinita, un cabezal lector y una tabla de estados. Este modelo abstracto definió qué problemas puede resolver cualquier computadora moderna.",
                    "Alan Turing formalized algorithms and computability with an infinite tape, a read/write head, and a state table. This abstract model defined the exact boundaries of what any modern computer can compute.",
                    "floor_02", 0xFF5A2138, 0xFFD98A73,
                    new String[]{"CINTA", "ESTADO", "REGLA", "SIGNO", "MENTE", "CLAVE", "VALOR", "CERO"},
                    new String[]{"TAPE", "STATE", "RULE", "MIND", "PROOF", "ZERO", "READ", "HEAD"}
            ),
            new PisoInfo(
                    3,
                    "El Transistor y el Binario", "Transistor & Binary",
                    "Laboratorios Bell", "Bell Labs",
                    PisoInfo.AREA_CS, "1947",
                    "El invento del transistor reemplazó las frágiles válvulas de vacío por interruptores diminutos capaces de alternar entre 0 y 1 millones de veces por segundo, abriendo la puerta a los microprocesadores modernos.",
                    "The invention of the transistor replaced fragile vacuum tubes with microscopic switches toggling between 0 and 1 millions of times per second, paving the way for modern microprocessors.",
                    "floor_03", 0xFF742531, 0xFFFFAA82,
                    new String[]{"BIT", "BYTE", "CERO", "PULSO", "CHIP", "FLUJO", "CARGA", "RED"},
                    new String[]{"BIT", "BYTE", "CHIP", "GATE", "PULSE", "WIRE", "ZERO", "LOGIC"}
            ),
            new PisoInfo(
                    4,
                    "Tennis for Two y Spacewar!", "Tennis for Two & Spacewar!",
                    "Pioneros del Juego Digital", "Digital Game Pioneers",
                    PisoInfo.AREA_VIDEOJUEGOS, "1958",
                    "En un osciloscopio de laboratorio en 1958 nació Tennis for Two, y pocos años después en el MIT surgió Spacewar! en un PDP-1. Dos experimentos científicos demostraron que las computadoras también podían emocionar jugando.",
                    "On a 1958 laboratory oscilloscope Tennis for Two was born, followed shortly by Spacewar! on an MIT PDP-1. Two scientific experiments proved computers could spark joy and competition through play.",
                    "floor_04", 0xFF4A1F33, 0xFFE3947A,
                    new String[]{"NAVE", "BOLA", "RAYO", "PANTALLA", "DUELO", "GIRO", "ORBITA", "PUNTO"},
                    new String[]{"SHIP", "BALL", "BEAM", "ORBIT", "DUEL", "SPIN", "SCORE", "SPACE"}
            ),
            new PisoInfo(
                    5,
                    "Dijkstra y los Grafos", "Dijkstra & Graphs",
                    "Caminos Más Cortos", "Shortest Paths",
                    PisoInfo.AREA_CS, "1959",
                    "Edsger Dijkstra ideó en apenas veinte minutos su algoritmo para hallar la ruta mínima entre nodos de un grafo. Hoy impulsa desde GPS hasta la navegación de personajes en videojuegos y el tablero hexagonal de Cletter.",
                    "Edsger Dijkstra devised his shortest-path graph algorithm in roughly twenty minutes. Today it powers everything from GPS routing to game pathfinding and Cletter's own hexagonal board.",
                    "floor_05", 0xFF64223A, 0xFFF29C79,
                    new String[]{"NODO", "GRAFO", "RUTA", "ARISTA", "PESO", "MAPA", "CAMINO", "RED"},
                    new String[]{"NODE", "GRAPH", "PATH", "EDGE", "ROUTE", "COST", "LINK", "MAP"}
            ),
            new PisoInfo(
                    6,
                    "Pong y la Era Arcade", "Pong & Golden Arcade",
                    "Atari y los Salones", "Atari & Arcades",
                    PisoInfo.AREA_VIDEOJUEGOS, "1972",
                    "Con dos barras blancas, un punto luminoso y una regla universal —'Evita perder la bola para sumar puntos'—, Pong inauguró la industria comercial del videojuego y llenó los salones recreativos de monedas.",
                    "With two white paddles, a glowing ball, and a single instruction—'Avoid missing ball for high score'—Pong launched the commercial video game industry and filled arcades with quarters.",
                    "floor_06", 0xFF7B2435, 0xFFFFB28B,
                    new String[]{"PONG", "FICHA", "PALA", "MARCA", "SALON", "RETRO", "NIVEL", "VIDA"},
                    new String[]{"PONG", "COIN", "SCORE", "RETRO", "LEVEL", "PLAY", "ARCADE", "BALL"}
            ),
            new PisoInfo(
                    7,
                    "El Lenguaje C y UNIX", "C Language & UNIX",
                    "Dennis Ritchie y Ken Thompson", "Dennis Ritchie & Ken Thompson",
                    PisoInfo.AREA_CS, "1972",
                    "C permitió escribir sistemas operativos portables con control directo de la memoria y la elegancia del código estructurado. Junto a UNIX, sentó las bases de casi todos los sistemas, compiladores y motores modernos.",
                    "C made it possible to write portable operating systems with direct memory control and structured elegance. Alongside UNIX, it laid the foundation for modern operating systems, compilers, and game engines.",
                    "floor_07", 0xFF531E2D, 0xFFD48972,
                    new String[]{"UNIX", "TIPO", "DATO", "BUCLE", "TEXTO", "LINEA", "ARCHIVO", "CODIGO"},
                    new String[]{"UNIX", "TYPE", "LOOP", "CHAR", "STACK", "FILE", "CODE", "SHELL"}
            ),
            new PisoInfo(
                    8,
                    "Pac-Man e IA en Enemigos", "Pac-Man & Ghost AI",
                    "Toru Iwatani", "Toru Iwatani",
                    PisoInfo.AREA_VIDEOJUEGOS, "1980",
                    "Cada fantasma de Pac-Man tiene una personalidad distinta programada con reglas simples de persecución y emboscada. Fue uno de los primeros ejemplos brillantes de diseño de comportamiento emergente en IA para juegos.",
                    "Each ghost in Pac-Man follows a distinct personality crafted from simple chase and ambush rules—one of the earliest and most beloved examples of emergent AI design in games.",
                    "floor_08", 0xFF7A2630, 0xFFFFB07C,
                    new String[]{"MAPA", "FRUTA", "PERSECUCION", "TURNO", "LABERINTO", "META", "PUNTO", "JUEGO"},
                    new String[]{"MAZE", "GHOST", "CHASE", "FRUIT", "DOTS", "STAGE", "POWER", "TURN"}
            ),
            new PisoInfo(
                    9,
                    "Tetris: Geometría y Flujo", "Tetris: Geometry & Flow",
                    "Alexey Pajitnov", "Alexey Pajitnov",
                    PisoInfo.AREA_VIDEOJUEGOS, "1984",
                    "Siete tetrominós cayendo en una matriz de 10×20 crearon el rompecabezas más universal de la historia. Su diseño demuestra cómo la simplicidad matemática puede generar un estado puro de concentración.",
                    "Seven falling tetrominoes inside a 10×20 grid created the most universal puzzle in history, proving how mathematical simplicity can unlock pure cognitive flow.",
                    "floor_09", 0xFF5D213B, 0xFFED9A7E,
                    new String[]{"PIEZA", "LINEA", "BLOQUE", "GIRO", "FORMA", "CAIDA", "RITMO", "BORDE"},
                    new String[]{"BLOCK", "LINE", "SHAPE", "DROP", "GRID", "CLEAR", "SPIN", "FLOW"}
            ),
            new PisoInfo(
                    10,
                    "8-Bits y el Pixel Art", "8-Bit & Pixel Art",
                    "Restricción como Arte", "Constraints as Art",
                    PisoInfo.AREA_VIDEOJUEGOS, "1985",
                    "Con apenas unos kilobytes de memoria y paletas de pocos colores por sprite, los artistas de la era 8-bits crearon personajes y bandas sonoras inolvidables que definieron el lenguaje visual del videojuego.",
                    "With just a few kilobytes of memory and tiny color palettes per sprite, 8-bit artists crafted unforgettable characters and chiptune melodies that defined gaming's visual language.",
                    "floor_10", 0xFF6F1F34, 0xFFFFAA82,
                    new String[]{"PIXEL", "COLOR", "MAPA", "SALTO", "MUNDO", "HEROE", "SONIDO", "SPRITE"},
                    new String[]{"PIXEL", "COLOR", "WORLD", "JUMP", "HERO", "CHIP", "MUSIC", "SPRITE"}
            ),
            new PisoInfo(
                    11,
                    "La World Wide Web", "The World Wide Web",
                    "Tim Berners-Lee", "Tim Berners-Lee",
                    PisoInfo.AREA_CS, "1989",
                    "En el CERN, Tim Berners-Lee combinó hipertexto, URLs y el protocolo HTTP para tejer una red abierta de conocimiento global. Un espacio donde información, código y comunidades se conectan al instante.",
                    "At CERN, Tim Berners-Lee combined hypertext, URLs, and HTTP to weave an open global web of knowledge—connecting information, software, and communities instantaneously.",
                    "floor_11", 0xFF4D1E31, 0xFFE39B7E,
                    new String[]{"WEB", "RED", "ENLACE", "TEXTO", "NODO", "SITIO", "PAGINA", "DATOS"},
                    new String[]{"WEB", "LINK", "HTTP", "HTML", "PAGE", "SITE", "NODE", "DATA"}
            ),
            new PisoInfo(
                    12,
                    "Doom y el Raycasting 3D", "Doom & 3D Raycasting",
                    "John Carmack e id Software", "John Carmack & id Software",
                    PisoInfo.AREA_VIDEOJUEGOS, "1993",
                    "Mediante árboles BSP y optimizaciones matemáticas audaces, el motor de Doom llevó mundos tridimensionales fluidos a computadoras domésticas y popularizó los mods y el multijugador en red local.",
                    "Using BSP trees and daring mathematical optimizations, Doom's engine brought fast 3D worlds to home PCs while popularizing modding and local network multiplayer.",
                    "floor_12", 0xFF7E1D28, 0xFFFF9E78,
                    new String[]{"RAYO", "MOTOR", "MURO", "MAPA", "ARBOL", "NIVEL", "VELOZ", "ACCION"},
                    new String[]{"RAY", "WALL", "TREE", "FAST", "DOOM", "LEVEL", "MAP", "ENGINE"}
            ),
            new PisoInfo(
                    13,
                    "Árboles, Pilas y Colas", "Trees, Stacks & Queues",
                    "Estructuras de Datos", "Data Structures",
                    PisoInfo.AREA_CS, "Clásico",
                    "Las pilas deshacen pasos, las colas circulares ordenan turnos, las listas enlazan registros y los árboles binarios buscan en milisegundos. Son los cimientos invisibles que hacen posible todo software, incluido Cletter.",
                    "Stacks undo steps, circular queues schedule turns, linked lists connect records, and binary trees search in milliseconds—the invisible foundations behind every piece of software, including Cletter.",
                    "floor_13", 0xFF631E34, 0xFFF5A380,
                    new String[]{"PILA", "COLA", "ARBOL", "LISTA", "NODO", "RAIZ", "HOJA", "ORDEN"},
                    new String[]{"STACK", "QUEUE", "TREE", "LIST", "NODE", "ROOT", "LEAF", "SORT"}
            ),
            new PisoInfo(
                    14,
                    "La Revolución Poligonal 3D", "The 3D Polygon Era",
                    "Cámaras y Espacios Libres", "Cameras & Free Spaces",
                    PisoInfo.AREA_VIDEOJUEGOS, "1996",
                    "El salto de los sprites planos a los polígonos texturizados reinventó el control analógico y la cámara dinámica. Explorar un castillo o un valle en tres dimensiones se convirtió en un nuevo estándar narrativo.",
                    "The leap from flat sprites to textured polygons reinvented analog controls and dynamic cameras. Exploring castles and valleys in full 3D became a new storytelling standard.",
                    "floor_14", 0xFF58223C, 0xFFE6977B,
                    new String[]{"MALLA", "CUBO", "CAMARA", "PLANO", "LUCES", "FORMA", "ESPACIO", "VISTA"},
                    new String[]{"MESH", "CUBE", "LIGHT", "SPACE", "VIEW", "PLANE", "SHAPE", "DEPTH"}
            ),
            new PisoInfo(
                    15,
                    "Software Libre y Linux", "Free Software & Linux",
                    "Linus Torvalds y la Comunidad", "Linus Torvalds & Community",
                    PisoInfo.AREA_CS, "1991",
                    "Lo que comenzó como un proyecto universitario compartido en un grupo de noticias creció hasta impulsar servidores, supercomputadoras y miles de millones de teléfonos Android en todo el mundo.",
                    "What started as a university hobby shared on a newsgroup grew to power servers, supercomputers, and billions of Android phones around the world.",
                    "floor_15", 0xFF4F2430, 0xFFEC9F7E,
                    new String[]{"LINUX", "LIBRE", "KERNEL", "FUENTE", "RAMAS", "PARCELA", "RED", "CODIGO"},
                    new String[]{"LINUX", "OPEN", "FREE", "CORE", "FORK", "CODE", "PATCH", "BUILD"}
            ),
            new PisoInfo(
                    16,
                    "Mundos Abiertos y Procedural", "Open Worlds & Procedural",
                    "Semillas Infinitas", "Infinite Seeds",
                    PisoInfo.AREA_VIDEOJUEGOS, "2000s",
                    "Mediante funciones de ruido y semillas deterministas, los algoritmos aprendieron a esculpir montañas, mazmorras y galaxias enteras sobre la marcha, haciendo cada partida única e irrepetible.",
                    "Using noise functions and deterministic seeds, algorithms learned to sculpt mountains, dungeons, and entire galaxies on the fly—making every journey unique.",
                    "floor_16", 0xFF6A2535, 0xFFFFB38A,
                    new String[]{"SEMILLA", "MUNDO", "BIOMA", "MAPA", "RUIDO", "EXPLORA", "NORTE", "VALLE"},
                    new String[]{"SEED", "WORLD", "BIOME", "NOISE", "QUEST", "NORTH", "CRAFT", "OPEN"}
            ),
            new PisoInfo(
                    17,
                    "Shaders, GPUs y Ray Tracing", "Shaders, GPUs & Ray Tracing",
                    "Gráficos en Paralelo", "Parallel Graphics",
                    PisoInfo.AREA_MIXTO, "2001",
                    "Las tarjetas gráficas evolucionaron en supercomputadoras paralelas capaces de calcular millones de píxeles, reflejos y rebotes de luz en tiempo real gracias a pequeños programas llamados shaders.",
                    "Graphics cards evolved into parallel supercomputers calculating millions of pixels, reflections, and light bounces in real time through tiny programs called shaders.",
                    "floor_17", 0xFF721E3A, 0xFFFFA880,
                    new String[]{"LUZ", "RAYO", "BRILLO", "SOMBRA", "COLOR", "VECTOR", "PIXEL", "FRAME"},
                    new String[]{"LIGHT", "RAY", "GLOW", "SHADE", "COLOR", "FRAME", "PIXEL", "GPU"}
            ),
            new PisoInfo(
                    18,
                    "El Renacimiento Indie", "The Indie Renaissance",
                    "Equipos Pequeños, Grandes Ideas", "Small Teams, Bold Ideas",
                    PisoInfo.AREA_VIDEOJUEGOS, "2010s",
                    "Motores accesibles y distribución digital permitieron que equipos pequeños crearan obras autorales con mecánicas experimentales, música artesanal y narrativas profundamente humanas.",
                    "Accessible engines and digital storefronts empowered tiny teams to craft auteur works filled with experimental mechanics, handcrafted music, and deeply human stories.",
                    "floor_18", 0xFF5B1F30, 0xFFF2A483,
                    new String[]{"INDIE", "ARTE", "IDEA", "ALMA", "HISTORIA", "RITMO", "JUEGO", "OBRA"},
                    new String[]{"INDIE", "ART", "IDEA", "SOUL", "STORY", "CRAFT", "PLAY", "WORK"}
            ),
            new PisoInfo(
                    19,
                    "Redes Neuronales e IA", "Neural Networks & AI",
                    "Aprendizaje Profundo", "Deep Learning",
                    PisoInfo.AREA_CS, "2012+",
                    "Inspiradas en la conectividad del cerebro y potenciadas por GPUs, las redes neuronales profundas aprendieron a reconocer imágenes, traducir lenguajes y colaborar en la creación de software y mundos virtuales.",
                    "Inspired by biological connectivity and accelerated by GPUs, deep neural networks learned to recognize images, translate languages, and collaborate in building software and virtual worlds.",
                    "floor_19", 0xFF6B1D2E, 0xFFFFAA82,
                    new String[]{"RED", "CAPA", "PESO", "DATOS", "MODELO", "SESGO", "APRENDE", "MENTE"},
                    new String[]{"LAYER", "MODEL", "TRAIN", "DATA", "WEIGHT", "NEURAL", "SMART", "MIND"}
            ),
            new PisoInfo(
                    20,
                    "Computación Cuántica", "Quantum Computing",
                    "El Futuro Jugable", "The Playable Future",
                    PisoInfo.AREA_MIXTO, "Futuro",
                    "Mientras los bits clásicos eligen 0 o 1, los cúbits explotan la superposición y el entrelazamiento para explorar estados en paralelo. El próximo horizonte de algoritmos, simulaciones físicas y universos interactivos.",
                    "While classical bits choose 0 or 1, qubits harness superposition and entanglement to explore states in parallel—the next horizon for algorithms, physics simulations, and interactive universes.",
                    "floor_20", 0xFF45172A, 0xFFFFB792,
                    new String[]{"QUBIT", "ONDA", "ESTADO", "PUERTA", "FUTURO", "CAMPO", "FISICA", "SALTO"},
                    new String[]{"QUBIT", "WAVE", "STATE", "GATE", "FUTURE", "FIELD", "QUANTUM", "LEAP"}
            )
    };

    private CatalogoPisos() {
    }

    public static PisoInfo[] obtenerTodos() {
        return PISOS;
    }

    public static PisoInfo obtenerPorNumero(int numeroPiso) {
        int seguro = Math.max(1, Math.min(TOTAL_PISOS, numeroPiso));
        return PISOS[seguro - 1];
    }

    public static PisoInfo obtenerPorNivel(int numeroNivel) {
        int nivelSeguro = Math.max(1, Math.min(TOTAL_NIVELES, numeroNivel));
        int piso = ((nivelSeguro - 1) / NIVELES_POR_PISO) + 1;
        return obtenerPorNumero(piso);
    }

    public static int calcularNumeroPiso(int numeroNivel) {
        int nivelSeguro = Math.max(1, Math.min(TOTAL_NIVELES, numeroNivel));
        return ((nivelSeguro - 1) / NIVELES_POR_PISO) + 1;
    }
}
