package com.alura.literalura.principal;

import com.alura.literalura.enumerations.Idioma;
import com.alura.literalura.model.*;
import com.alura.literalura.repository.AutorRepository;
import com.alura.literalura.repository.LibroRepository;
import com.alura.literalura.service.ConsumoAPI;
import com.alura.literalura.service.ConvierteDatos;

import java.util.*;


public class Principal {

    private static final String URL_BASE = "https://gutendex.com/books/";
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private Scanner teclado = new Scanner(System.in);
    private LibroRepository repositorioLibro;
    private AutorRepository repositorioAutor;

    public Principal(LibroRepository repositoryLibro, AutorRepository repositoryAutor) {
        this.repositorioLibro = repositoryLibro;
        this.repositorioAutor = repositoryAutor;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """

                    1 - Buscar libro por titulo 
                    2 - Mostrar todos los libros registrados
                    3 - Mostrar todos los autores registrados 
                    4 - Mostrar los autores vivos en determinado año 
                    5 - Lista de libros por idioma          
                    0 - Salir
                    
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarLibroTitulo();
                    break;
                case 2:
                    mostrarTodosLibros();
                    break;
                case 3:
                    mostrarTodosAutores();
                    break;
                case 4:
                    autorVivoAnio();
                    break;
                case 5:
                    librosIdioma();
                    break;
                case 0:
                    System.out.println("LA APLICACION SE CERRARA...");
                    break;
                default:
                    System.out.println("❌ OPCION NO VALIDA");
            }
        }

    }

    private void buscarLibroTitulo(){

        //Busqueda de libros por nombre
        System.out.println("Introduzca el titulo del libro que se va a buscar...");
        var tituloLibro = teclado.nextLine();
        var json = consumoAPI.obtenerDatos(URL_BASE+"?search=" + tituloLibro.replace(" ","+"));
        var datosBusqueda = conversor.obtenerDatos(json, Datos.class);
        List<Autor> autores = new ArrayList<>(); //Donde guardamos los autores.
        Optional<DatosLibro> libroBuscado = datosBusqueda.resultados().stream()
                .filter(l -> l.titulo().toUpperCase().contains(tituloLibro.toUpperCase()))
                .findFirst();

            if(libroBuscado.isPresent()){
                if (repositorioLibro.existsByTitulo(libroBuscado.get().titulo())) { //Buscamos si ya esta registrado en la base.
                    System.out.println("⚠️ El libro ya esta registrado!!\n");
                } else { //Si no esta registrado en la base.
                    System.out.println("✅ !!Libro encontrado con exito!!\n");
                    System.out.println("--------------------------------");
                    System.out.println("Titulo: "+libroBuscado.get().titulo());
                    System.out.print("Autor(es): ");
                    for (DatosAutor autor : libroBuscado.get().autor()) {
                        System.out.print(autor.nombre());
                        if (autor.fechaDeNacimiento() != null) {
                            System.out.print(" (" + autor.fechaDeNacimiento() + ")");
                        }
                        if (autor.fechaDeMuerte() != null) {
                            System.out.print(" (" + autor.fechaDeMuerte() + ")");
                        }
                        System.out.print("  ");

                        // PARA NO TENER AUTORES DUPLICADOS
                        Optional<Autor> autorExistente = repositorioAutor.findByNombre(autor.nombre());
                        Autor aut;
                        if (autorExistente.isPresent()) {
                            aut = autorExistente.get(); // Usar autor ya existente
                        } else {
                            aut = new Autor(autor); // Crear autor nuevo
                            aut = repositorioAutor.save(aut); //Guardar explicitamente el autor que es nuevo
                        }

                        autores.add(aut); //Agreganos el autor a la lista de autores.
                    }
                    System.out.println("Idiomas: " + libroBuscado.get().idiomas());
                    System.out.println("Descargas: " + libroBuscado.get().numeroDeDescargas());
                    System.out.println("-------------------------------------------");
                    //Guardado del libro en la base de datos.
                    Libro libro = new Libro(libroBuscado.get());
                    libro.setAutor(autores); //Asignamos la lista de autores al libro.
                    repositorioLibro.save(libro); //guardamos los autores en la base de datos.

                }

            }else {
                System.out.println("⚠️ El Libro no fue encontrado!!\n");
            }
    }

    private void mostrarTodosLibros(){

        List<Libro> libros = repositorioLibro.findAll();

        libros.stream().forEach(
                libro -> imprimeLibroAutor(libro)
        );

    }

    private void mostrarTodosAutores(){

        List<Autor> autores = repositorioAutor.findAll();

        autores.stream().forEach(
                autor -> imprimeLibroAutor(autor)
        );

    }

    private void autorVivoAnio() {
        System.out.println("Introduzca el Año: ");
        var anio = teclado.nextInt();

        List<Autor> autores = repositorioAutor.findAll();

        // Primero filtramos y guardamos en una lista temporal
        List<Autor> autoresVivos = autores.stream()
                .filter(autor -> {
                    try {
                        String nacimientoStr = autor.getFechaDeNacimiento(); //Si la fecha de nacimiento esta vacia (no existe)
                        if (nacimientoStr == null || nacimientoStr.trim().isEmpty())
                            {
                                return false;
                            }
                        int nacimiento = Integer.parseInt(nacimientoStr.trim());
                        String muerteStr = autor.getFechaDeMuerte();
                        int muerte = (muerteStr == null || muerteStr.trim().isEmpty())
                                ? Integer.MAX_VALUE
                                : Integer.parseInt(muerteStr.trim());
                        return anio >= nacimiento && anio < muerte;
                    } catch (NumberFormatException e) {
                        // Por si los datos se han pasado mal
                        return false;
                    }
                })
                .toList();

        if (autoresVivos.isEmpty()) {
            System.out.println("⚠️ No hay autores vivos en ese año.");
        } else {
            autoresVivos.forEach(this::imprimeLibroAutor);
        }
    }

    private void librosIdioma()
    {
        System.out.println("| Idiomas disponibles |");
        for (int i = 0; i < Idioma.values().length; i++) {
            System.out.printf("%d. %s (%s)%n", i + 1, Idioma.values()[i].getNombreCompleto(), Idioma.values()[i].name());
        }
        System.out.print("* Seleccione el número del idioma: ");
        int opcion = teclado.nextInt();
        teclado.nextLine(); // limpiar buffer
        if (opcion < 1 || opcion > Idioma.values().length) {
            System.out.println("❌ Opción inválida.");
            return;
        }
        Idioma idiomaSeleccionado = Idioma.values()[opcion - 1];

        List<Libro> librosFiltrados = repositorioLibro.findAll().stream()
                .filter(libro -> libro.getIdiomas().stream()
                        .anyMatch(idioma -> idioma.equalsIgnoreCase(idiomaSeleccionado.name())))
                .toList();

        int cantidad = librosFiltrados.size();

        if (librosFiltrados.isEmpty()) {
            System.out.println("⚠️ No hay libros en ese idioma.");
        } else {
            System.out.println("\n Cantidad de libros en el idioma " + idiomaSeleccionado.getNombreCompleto() + ": " + cantidad);
            librosFiltrados.forEach(this::imprimeLibroAutor);
        }

    }


    private <T> void imprimeLibroAutor(T clase){

        if (clase instanceof Libro libro) {
            System.out.println("-------------------------------------------");
            System.out.println("Título: " + libro.getTitulo());

            System.out.print("Autor(es): ");
            for (Autor autor : libro.getAutor()) {
                System.out.print(autor.getNombre());
                System.out.print(" (" + autor.getFechaDeNacimiento() + ")");
                System.out.print(" (" + autor.getFechaDeMuerte() + ")");
                System.out.print("  ");
            }
            System.out.println();
            System.out.println("Idiomas: " + libro.getIdiomas());
            System.out.println("Descargas: " + libro.getNumeroDeDescargas());
            System.out.println("-------------------------------------------");
        }else if (clase instanceof Autor autor){
            System.out.println("-------------------------------------------");
            System.out.println("Nombre del autor: "+ autor.getNombre());
            System.out.println("Fecha de Nacimiento: " + autor.getFechaDeNacimiento());
            System.out.println("Fecha de Muerte: " + autor.getFechaDeMuerte());
            System.out.print("Libros: ");
            for(Libro libro : autor.getLibros())
            {
                System.out.print(libro.getTitulo() + " | ");
            }
            System.out.println("\n-------------------------------------------");
        }
    }


}






