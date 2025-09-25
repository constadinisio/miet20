<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mi ET20 - Página Principal</title>
    <link rel="icon" type="image/x-icon" href="images/et20png.png">
    <link href="output.css?v=<?= time() ?>" rel="stylesheet">
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <style>
        body {
            font-family: 'Poppins', sans-serif;
        }

        .menu-item {
            display: flex;
            align-items: center;
            padding: 0.5rem 0.75rem;
            font-weight: 500;
            font-size: 1rem;
            border-radius: 0.375rem;
            transition: background-color 0.3s ease;
            text-decoration: none;
        }

        .menu-item:hover {
            background-color: #f3f4f6;
            /* bg-gray-100 */
        }

        .menu-icon {
            font-size: 1.5rem;
        }

        .menu-text {
            margin-left: 0.5rem;
            display: none;
            white-space: nowrap;
            animation: fadeSlide 0.5s ease forwards;
        }

        .menu-item:hover .menu-text {
            display: inline;
        }

        @keyframes fadeSlide {
            from {
                opacity: 0;
                transform: translateX(-6px);
            }

            to {
                opacity: 1;
                transform: translateX(0);
            }
        }
    </style>
</head>

<body class="bg-gray-50">
    <!-- Navbar -->
    <nav class="bg-white shadow-lg fixed w-full z-50">
        <div class="max-w-7xl mx-auto px-4">
            <div class="flex justify-between items-center h-16">
                <div class="flex items-center">
                    <a href="index.php" class="flex items-center">
                        <i class="fas text-3xl text-blue-600 mr-4 -right-500"></i>
                        <h1><img src="/images/et20png.png" alt="Icono personalizado" class="w-10 h-10"></h1>
                        <span class="text-xl font-semibold text-gray-800 ml-2">Escuela Técnica 20 D.E. 20</span>
                    </a>
                </div>

                <div class="hidden md:flex items-center gap-4">

                    <a href="index.php" class="menu-item text-azulInstitucional text-xl">
                        <span class="text-lg">🏠‎ ‎ Página Principal</span>
                    </a>

                    <a href="descargas.php" class="menu-item hover:text-gray-600 text-xl ">
                        <span class="menu-icon">📁</span>
                        <span class="menu-text">Descargas</span>
                    </a>

                    <a href="noticias.php" class="menu-item hover:text-gray-600 text-xl">
                        <span class="menu-icon">📰</span>
                        <span class="menu-text">Noticias</span>
                    </a>

                    <a href="galeria_home.php" class="menu-item hover:text-gray-600 text-xl">
                        <span class="menu-icon">📷</span>
                        <span class="menu-text">Galería</span>
                    </a>

                    <a href="contactos.php" class="menu-item hover:text-gray-600 text-xl">
                        <span class="menu-icon">📩</span>
                        <span class="menu-text">Contactos</span>
                    </a>
                </div>

                <a href="login.php" class="ml-8 px-5 py-2 rounded-xl bg-indigo-600 text-white hover:bg-indigo-700 font-semibold shadow">
                    Iniciar Sesión
                </a>

                <!-- Mobile Menu Button -->
                <div class="md:hidden flex items-center">
                    <button class="mobile-menu-button outline-none">
                        <i class="fas fa-bars text-2xl text-gray-600"></i>
                    </button>
                </div>
            </div>

            <!-- Mobile Menu -->
            <div id="mobile-menu" class="mobile-menu hidden md:hidden pb-4 flex flex-col space-y-1 transition-all duration-300 transform opacity-0 scale-95">
                <a href="index.php" class="block text-azulInstitucional px-4 py-2 rounded-md font-medium">Página Principal</a>
                <a href="descargas.php" class="block text-gray-600 hover:text-rojoDestacado px-4 py-2 rounded-md font-medium transition duration-300">Descargas</a>
                <a href="noticias.php" class="block text-gray-600 hover:text-verdeEsperanza px-4 py-2 rounded-md font-medium transition duration-300">Noticias</a>
                <a href="galeria_home.php" class="text-gray-600 hover:text-amarilloEnergia px-4 py-2 rounded-md font-medium transition duration-300">Galeria</a>
                <a href="contactos.php" class="text-gray-600 hover:text-rosaMagico px-4 py-2 rounded-md font-medium transition duration-300">Contactos</a>
            </div>
        </div>
    </nav>

    <!-- Hero Section -->
    <section class="relative h-screen w-full pt-16 text-white overflow-hidden">
        <!-- Imagen con blur -->
        <div class="absolute inset-0 bg-front-et20 bg-no-repeat bg-cover bg-center filter blur-sm scale-105"></div>

        <!-- Overlay opcional (oscurece un poco para mejorar legibilidad) -->
        <div class="absolute inset-0 bg-black/30"></div>

        <!-- Contenido -->
        <div class="relative z-10 max-w-7xl mx-auto px-4 py-20 md:py-32">
            <div class="text-center">
                <h2 class="text-4xl md:text-6xl font-bold mb-6">
                    Bienvenidos a la <br />
                    Escuela Técnica 20 D.E. 20<br />
                    "Carolina Muzilli"
                </h2>
                <p class="text-xl md:text-2xl mb-8">
                    Formando líderes del mañana con excelencia académica y valores
                </p>
            </div>
        </div>
    </section>

    <!-- Presentación del Colegio -->
    <section class="py-16 bg-white">
        <div class="max-w-7xl mx-auto px-4">
            <h2 class="text-3xl font-bold text-center mb-12">Nuestra Institución</h2>
            <div class="grid md:grid-cols-2 gap-12 items-center">
                <div>
                    <img src="/images/front_et20.webp" alt="Fachada del colegio" class="rounded-lg shadow-lg">
                </div>
                <div>
                    <h3 class="text-2xl font-semibold mb-4">Excelencia Educativa</h3>
                    <p class="text-gray-600 mb-6">
                        La Escuela Técnica N.º 20 D.E. 20 “Carolina Muzzilli” se destaca por ofrecer una educación integral que combina excelencia académica con una sólida formación de profesionales.
                        Nuestro compromiso es preparar a los y las estudiantes para los desafíos del futuro, fomentando el pensamiento crítico, la responsabilidad social y la innovación tecnológica.
                        Formamos técnicos con conciencia ciudadana, preparados para contribuir activamente en la sociedad y el mundo laboral.
                    </p>
                    <div class="grid grid-cols-2 gap-4">
                        <div class="bg-blue-50 p-4 rounded-lg">
                            <i class="fas fa-users text-blue-600 text-2xl mb-2"></i>
                            <h4 class="font-semibold">Comunidad</h4>
                            <p class="text-sm text-gray-600">Más de 700 estudiantes</p>
                        </div>
                        <div class="bg-blue-50 p-4 rounded-lg">
                            <i class="fas fa-award text-blue-600 text-2xl mb-2"></i>
                            <h4 class="font-semibold">Reconocimientos</h4>
                            <p class="text-sm text-gray-600">Excelencia educativa</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- Especialidades -->
    <section class="py-16 bg-gray-50">
        <div class="max-w-7xl mx-auto px-4">
            <h2 class="text-3xl font-bold text-center mb-12">Nuestras Especialidades</h2>
            <div class="grid md:grid-cols-2 gap-8">
                <!-- Producción Multimedial -->

                <a href="./especialidad_mult.html">
                    <div class="bg-white rounded-lg shadow-lg overflow-hidden transition duration-300 hover:shadow-xl">
                        <img src="./images/set_mult.webp" alt="Laboratorio de ciencias" class="w-full h-48 object-cover">
                        <div class="p-6">
                            <h3 class="text-xl font-semibold mb-2">Producción Multimedial</h3>
                            <p class="text-gray-600">
                                Especialidad centrada en diseño multimedial, animación, audio, video y producción digital.
                            </p>
                        </div>
                    </div>
                </a>

                <!-- Tecnologías de la Información y la Comunicación -->
                <a href="./especialidad_tics.html">
                    <div class="bg-white rounded-lg shadow-lg overflow-hidden transition duration-300 hover:shadow-xl">
                        <img src="/images/ing_tics.webp" alt="Biblioteca" class="w-full h-48 object-cover">
                        <div class="p-6">
                            <h3 class="text-xl font-semibold mb-2">Tecnologías de la Información y la Comunicación</h3>
                            <p class="text-gray-600">
                                Especialidad enfocada en informática, redes, programación, soporte técnico y comunicación digital.
                            </p>
                        </div>
                    </div>
                </a>
            </div>
        </div>
    </section>

    <!-- Galería de Imágenes -->
    <section class="py-12 bg-gray-50">
        <div class="max-w-7xl mx-auto px-4">
            <h2 class="text-3xl font-bold text-center mb-6">Galería de Imágenes</h2>
            <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
                <img src="/images/5.JPG" alt="Actividades escolares" class="rounded-lg shadow-md hover:shadow-xl transition duration-300">
                <img src="/images/16.JPG" alt="Estudiantes en clase" class="rounded-lg shadow-md hover:shadow-xl transition duration-300">
                <img src="/images/24.JPG" alt="Laboratorio" class="rounded-lg shadow-md hover:shadow-xl transition duration-300">
                <img src="/images/DSC_2365.jpg" alt="Biblioteca" class="rounded-lg shadow-md hover:shadow-xl transition duration-300">
            </div>
            <div class="flex justify-center my-10">
                <a href="galeria_home.php" class="bg-blue-600 text-white px-4 py-1 rounded-full text-lg shadow-lg hover:bg-blue-900 transition">
                    Ver Más de la Galeria
                </a>
            </div>
        </div>
    </section>

    <!-- Footer -->
    <footer class="bg-gray-800 text-white py-12">
        <div class="max-w-7xl mx-auto px-4">
            <div class="grid md:grid-cols-3 gap-8">
                <div>
                    <h3 class="text-xl font-semibold mb-4">Escuela Técnica 20 D.E. 20<br>"Carolina Muzilli"</h3>
                    <p class="text-gray-400">
                        Formando líderes del mañana con excelencia académica y valores.
                    </p>
                </div>
                <div>
                    <h3 class="text-xl font-semibold mb-4">Contacto</h3>
                    <p class="text-gray-400">
                        <i class="fas fa-map-marker-alt mr-2"></i> Murguiondo 2151, CABA
                        <br>
                        <i class="fas fa-phone mr-2"></i> (54) 113910-7733
                        <br>
                        <i class="fas fa-envelope mr-2"></i> det_20_de20@bue.edu.ar
                    </p>
                </div>
                <div>
                    <h3 class="text-xl font-semibold mb-4">Síguenos</h3>
                    <div class="flex space-x-4">
                        <a href="https://www.youtube.com/@ETDEPoloMataderos-Verificacion/featured" rel="noopener noreferrer" class="text-gray-400 hover:text-white transition duration-300">
                            <i class="fa-brands fa-youtube text-2xl"></i>
                        </a>
                        <a href="https://www.instagram.com/et20polomataderos/" rel="noopener noreferrer" class="text-gray-400 hover:text-white transition duration-300">
                            <i class="fab fa-instagram text-2xl"></i>
                        </a>
                    </div>
                </div>
            </div>
            <div class="border-t border-gray-700 mt-8 pt-8 text-center text-gray-400">
                <p>&copy; 2024 Escuela Técnica 20 D.E. 20 "Carolina Muzilli". Todos los derechos reservados.</p>
            </div>
        </div>
    </footer>

    <!-- JavaScript for Mobile Menu -->
    <script>
        document.addEventListener('DOMContentLoaded', () => {
            const btn = document.querySelector('.mobile-menu-button');
            const menu = document.getElementById('mobile-menu');

            btn.addEventListener('click', () => {
                const isHidden = menu.classList.contains('hidden');

                if (isHidden) {
                    // Mostrar con animación
                    menu.classList.remove('hidden');
                    // Necesario para que transition corra después del reflow
                    requestAnimationFrame(() => {
                        menu.classList.remove('opacity-0', 'scale-95');
                        menu.classList.add('opacity-100', 'scale-100');
                    });
                } else {
                    // Ocultar con animación
                    menu.classList.remove('opacity-100', 'scale-100');
                    menu.classList.add('opacity-0', 'scale-95');
                    // Después del tiempo de transición, ocultar completamente
                    setTimeout(() => {
                        menu.classList.add('hidden');
                    }, 300);
                }
            });
        });
    </script>

    <!-- Botón flotante de WhatsApp -->
    <a href="https://wa.me/5491139107733" target="_blank"
        class="fixed bottom-4 right-4 z-50 bg-green-500 hover:bg-green-600 text-white p-4 rounded-full shadow-lg transition">
        <img src="/images/whatsapp_icon.svg" alt="WhatsApp" class="w-8 h-8">
    </a>

</body>

</html>