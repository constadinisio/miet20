<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mi ET20 - Descargas</title>
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
            animation: fadeSlide 0.3s ease forwards;
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
                        <h1><img src="./images/et20png.png" alt="Icono personalizado" class="w-10 h-10"></h1>
                        <span class="text-xl font-semibold text-gray-800 ml-2">Escuela Técnica 20 D.E. 20</span>
                    </a>
                </div>

                <div class="hidden md:flex items-center gap-4">

                    <a href="index.php" class="menu-item hover:text-gray-600 text-xl">
                        <span class="menu-icon">🏠</span>
                        <span class="menu-text">Página Principal</span>
                    </a>

                    <a href="descargas.php" class="menu-item text-rojoDestacado text-xl ">
                        <span class="text-lg">📁‎ ‎ Descargas</span>
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

    <!-- Header Section -->
    <section class="pt-24 pb-12 bg-gradient-to-r from-blue-500 to-blue-700 text-white">
        <div class="max-w-7xl mx-auto px-4">
            <h1 class="text-4xl font-bold text-center">Descargas</h1>
            <p class="text-center mt-4">Accede a documentos importantes y recursos educativos</p>
        </div>
    </section>

    <!-- Downloads Section -->
    <section class="py-16">
        <div class="max-w-7xl mx-auto px-4">
            <div class="grid md:grid-cols-1 gap-8">
                <!-- Aplicación -->
                <div class="bg-white rounded-lg shadow-lg p-6">
                    <h2 class="text-2xl font-semibold mb-6">Plataforma de Gestión de Escuela</h2>
                    <div class="space-y-4">
                        <a href="#" class="flex items-center p-4 border rounded-lg hover:bg-gray-50 transition duration-300">
                            <i class="fa-solid fa-laptop text-orange-500 text-2xl mr-4"></i>
                            <div>
                                <h3 class="font-medium">Mi ET20 <strong>(PROXIMAMENTE...)</strong></h3>
                                <p class="text-sm text-gray-600">EXE - 143 MB</p>
                            </div>
                            <i class="fas fa-download ml-auto text-gray-400"></i>
                        </a>
                    </div>
                </div>
            </div>
            <div class="grid md:grid-cols-1 gap-8">
                <!-- Documentos -->
                <div class="bg-white rounded-lg shadow-lg p-6">
                    <h2 class="text-2xl font-semibold mb-6">Documentos Institucionales</h2>
                    <div class="space-y-4">
                        <a href="./docs/const_alumno_reg.pdf" download="Constancia Alumno Regular" class="flex items-center p-4 border rounded-lg hover:bg-gray-50 transition duration-300">
                            <i class="fas fa-file-pdf text-red-500 text-2xl mr-4"></i>
                            <div>
                                <h3 class="font-medium">Constancia Alumno Regular</h3>
                                <p class="text-sm text-gray-600">PDF - 43 KB</p>
                            </div>
                            <i class="fas fa-download ml-auto text-gray-400"></i>
                        </a>
                        <a href="./docs/curricula_tics.pdf" download="Curricula de TICS @ 2025" class="flex items-center p-4 border rounded-lg hover:bg-gray-50 transition duration-300">
                            <i class="fas fa-file-pdf text-red-500 text-2xl mr-4"></i>
                            <div>
                                <h3 class="font-medium">Curricula - TICs</h3>
                                <p class="text-sm text-gray-600">PDF - 132 KB</p>
                            </div>
                            <i class="fas fa-download ml-auto text-gray-400"></i>
                        </a>
                        <a href="./docs/curricula_mutimedia.pdf" download="Curricula de Multimedia @ 2025" class="flex items-center p-4 border rounded-lg hover:bg-gray-50 transition duration-300">
                            <i class="fas fa-file-pdf text-red-500 text-2xl mr-4"></i>
                            <div>
                                <h3 class="font-medium">Curricula - Multimedia</h3>
                                <p class="text-sm text-gray-600">PDF - 171 KB</p>
                            </div>
                            <i class="fas fa-download ml-auto text-gray-400"></i>
                        </a>
                        <a href="./docs/regimen_academico.pdf" download="Regimen Academico ET20" class="flex items-center p-4 border rounded-lg hover:bg-gray-50 transition duration-300">
                            <i class="fas fa-file-pdf text-red-500 text-2xl mr-4"></i>
                            <div>
                                <h3 class="font-medium">Regimen Academico</h3>
                                <p class="text-sm text-gray-600">PDF - 276 KB</p>
                            </div>
                            <i class="fas fa-download ml-auto text-gray-400"></i>
                        </a>
                        <a href="./docs/regimen_aprobacion.pdf" download="Regimen de Aprobación" class="flex items-center p-4 border rounded-lg hover:bg-gray-50 transition duration-300">
                            <i class="fas fa-file-pdf text-red-500 text-2xl mr-4"></i>
                            <div>
                                <h3 class="font-medium">Regimen Aprobacion</h3>
                                <p class="text-sm text-gray-600">PDF - 29 KB</p>
                            </div>
                            <i class="fas fa-download ml-auto text-gray-400"></i>
                        </a>
                    </div>
                </div>
            </div>

            <!-- Información Adicional -->
            <div class="mt-12 bg-blue-50 rounded-lg p-6">
                <h2 class="text-xl font-semibold mb-4">Información Importante</h2>
                <ul class="list-disc list-inside text-gray-700 space-y-2">
                    <li>Todos los documentos están en formato digital para su fácil acceso</li>
                    <li>Para problemas con las descargas, contacte al soporte técnico</li>
                    <li>Los archivos se actualizan periódicamente</li>
                </ul>
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
</body>

</html>