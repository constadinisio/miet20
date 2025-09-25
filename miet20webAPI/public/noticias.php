<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mi ET20 - Noticias</title>
    <link rel="icon" type="image/x-icon" href="images/et20png.png">
    <link href="output.css?v=<?= time() ?>" rel="stylesheet">


    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap"
        rel="stylesheet">
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

                    <a href="descargas.php" class="menu-item hover:text-gray-600 text-xl ">
                        <span class="menu-icon">📁</span>
                        <span class="menu-text">Descargas</span>
                    </a>

                    <a href="noticias.php" class="menu-item text-verdeEsperanza text-xl">
                        <span class="text-lg">📰‎ ‎ Noticias</span>
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
            <h1 class="text-4xl font-bold text-center">Noticias</h1>
            <p class="text-center mt-4">Mantente informado sobre las últimas novedades del colegio</p>

        </div>
    </section>

    <!-- News Section -->
    <div class="max-w-4xl mx-auto">
        <div id="contenedor-noticias" class="space-y-6"></div>
        <div class="flex justify-center mt-6 space-x-2" id="paginacion"></div>
    </div>

    <script>
        const noticiasPorPagina = 5;
        let paginaActual = 1;
        let noticias = [];

        fetch('/panelNoticias/data/noticias.json')
            .then(res => res.json())
            .then(data => {
                noticias = data.reverse();
                mostrarPagina(paginaActual);
                crearPaginacion();
            });

        function mostrarPagina(pagina) {
            const contenedor = document.getElementById('contenedor-noticias');
            contenedor.innerHTML = "";
            const inicio = (pagina - 1) * noticiasPorPagina;
            const fin = inicio + noticiasPorPagina;
            const paginaNoticias = noticias.slice(inicio, fin);

            paginaNoticias.forEach(noticia => {
                const div = document.createElement('div');
                div.className = "bg-white p-4 m-4 shadow rounded";

                const titulo = document.createElement('h2');
                titulo.className = "text-xl font-bold mb-2";
                titulo.innerText = noticia.titulo;

                const contenido = document.createElement('div');
                contenido.className = "contenido-noticia prose max-w-none";
                console.log(noticia.contenido)
                contenido.innerHTML = noticia.contenido;

                let imagen = null;
                if (noticia.hasOwnProperty('imagen') && noticia.imagen.trim() !== "") {
                    imagen = document.createElement('img');
                    imagen.src = `/panelNoticias/images/${noticia.imagen}`;
                    imagen.alt = "Imagen de la noticia";
                    imagen.className = "mb-4 max-w-64 h-auto rounded";
                }

                div.appendChild(titulo);
                div.appendChild(contenido);
                if (imagen) div.appendChild(imagen);

                contenedor.appendChild(div);
            });
        }

        function crearPaginacion() {
            const totalPaginas = Math.ceil(noticias.length / noticiasPorPagina);
            const paginacion = document.getElementById('paginacion');
            paginacion.innerHTML = "";
            for (let i = 1; i <= totalPaginas; i++) {
                const boton = document.createElement('button');
                boton.innerText = i;
                boton.className = "px-3 py-1 rounded m-4 " + (i === paginaActual ? "bg-blue-600 text-white" : "bg-gray-300");
                boton.onclick = () => {
                    paginaActual = i;
                    mostrarPagina(i);
                    crearPaginacion();
                };
                paginacion.appendChild(boton);
            }
        }
    </script>

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
                        <a href="https://www.youtube.com/@ETDEPoloMataderos-Verificacion/featured"
                            rel="noopener noreferrer" class="text-gray-400 hover:text-white transition duration-300">
                            <i class="fa-brands fa-youtube text-2xl"></i>
                        </a>
                        <a href="https://www.instagram.com/et20polomataderos/" rel="noopener noreferrer"
                            class="text-gray-400 hover:text-white transition duration-300">
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
        const mobileMenuButton = document.querySelector('.mobile-menu-button');
        const mobileMenu = document.querySelector('.mobile-menu');

        mobileMenuButton.addEventListener('click', () => {
            mobileMenu.classList.toggle('hidden');
        });
    </script>
</body>

</html>