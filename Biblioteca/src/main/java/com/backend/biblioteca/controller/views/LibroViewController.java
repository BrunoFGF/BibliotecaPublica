package com.backend.biblioteca.controller.views;

import com.backend.biblioteca.model.Libro;
import com.backend.biblioteca.service.LibroService;
import com.backend.biblioteca.dto.request.LibroCreateRequest;
import com.backend.biblioteca.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/web/libros")
public class LibroViewController {

    @Autowired
    private LibroService libroService;

    @GetMapping
    public String listarLibros(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(required = false) String codigoLibro,
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String autor,
            @RequestParam(required = false) String editorial,
            @RequestParam(required = false) Integer anioPublicacion,
            @RequestParam(required = false) String search,
            Model model) {

        var response = libroService.searchLibros(
                page, size, sortBy, sortDirection,
                codigoLibro, titulo, autor, editorial,
                anioPublicacion, null, null, search
        );

        model.addAttribute("libros", response);
        model.addAttribute("currentPage", page);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("search", search != null ? search : "");

        return "libros/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("libro", new LibroCreateRequest());
        model.addAttribute("action", "crear");
        return "libros/formulario";
    }

    @PostMapping("/crear")
    public String crearLibro(@Valid @ModelAttribute("libro") LibroCreateRequest libro,
                             BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("action", "crear");
            return "libros/formulario";
        }

        try {
            libroService.createLibro(libro);
            redirectAttributes.addFlashAttribute("mensaje", "Libro creado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/web/libros";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Libro libro = libroService.getLibroById(id);
            LibroCreateRequest libroRequest = new LibroCreateRequest();
            libroRequest.setCodigoLibro(libro.getCodigoLibro());
            libroRequest.setTitulo(libro.getTitulo());
            libroRequest.setAutor(libro.getAutor());
            libroRequest.setEditorial(libro.getEditorial());
            libroRequest.setAnioPublicacion(libro.getAnioPublicacion());
            libroRequest.setCantidadDisponible(libro.getCantidadDisponible());

            model.addAttribute("libro", libroRequest);
            model.addAttribute("libroId", id);
            model.addAttribute("action", "editar");
            return "libros/formulario";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("mensaje", "Libro no encontrado");
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/web/libros";
        }
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarLibro(@PathVariable Long id,
                                  @Valid @ModelAttribute("libro") LibroCreateRequest libro,
                                  BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("libroId", id);
            model.addAttribute("action", "editar");
            return "libros/formulario";
        }

        try {
            libroService.updateLibro(id, libro);
            redirectAttributes.addFlashAttribute("mensaje", "Libro actualizado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/web/libros";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarLibro(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            libroService.deleteLibro(id);
            redirectAttributes.addFlashAttribute("mensaje", "Libro eliminado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/web/libros";
    }

    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Libro libro = libroService.getLibroById(id);
            model.addAttribute("libro", libro);
            return "libros/detalle";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("mensaje", "Libro no encontrado");
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/web/libros";
        }
    }
}