package com.backend.biblioteca.controller.views;

import com.backend.biblioteca.dto.request.ChangePasswordRequest;
import com.backend.biblioteca.dto.request.UsuarioCreateRequest;
import com.backend.biblioteca.dto.request.UsuarioUpdateRequest;
import com.backend.biblioteca.dto.response.UsuarioResponse;
import com.backend.biblioteca.exception.ResourceNotFoundException;
import com.backend.biblioteca.model.Rol;
import com.backend.biblioteca.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/web/usuarios")
@PreAuthorize("hasRole('BIBLIOTECARIO')")
public class UsuarioViewController {

    @Autowired
    private UsuarioService usuarioService;

    // Página principal - Lista de usuarios
    @GetMapping
    public String listarUsuarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String cedula,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String search,
            Model model) {

        var response = usuarioService.searchUsuarios(
                page, size, sortBy, sortDirection,
                nombre, cedula, email, rol, activo, search
        );

        List<Rol> roles = usuarioService.getAllRoles();

        model.addAttribute("usuarios", response);
        model.addAttribute("roles", roles);
        model.addAttribute("currentPage", page);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("search", search != null ? search : "");

        return "usuarios/lista";
    }

    // Formulario para crear usuario
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        UsuarioCreateRequest usuario = new UsuarioCreateRequest();
        List<Rol> roles = usuarioService.getAllRoles();

        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", roles);
        model.addAttribute("action", "crear");
        return "usuarios/formulario";
    }

    // Crear usuario
    @PostMapping("/crear")
    public String crearUsuario(@Valid @ModelAttribute("usuario") UsuarioCreateRequest usuario,
                               BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            List<Rol> roles = usuarioService.getAllRoles();
            model.addAttribute("roles", roles);
            model.addAttribute("action", "crear");
            return "usuarios/formulario";
        }

        try {
            usuarioService.createUsuario(usuario);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario creado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/web/usuarios";
    }

    // Eliminar usuario
    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.deleteUsuario(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/web/usuarios";
    }

    // Ver detalles del usuario
    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            UsuarioResponse usuario = usuarioService.getUsuarioById(id);
            model.addAttribute("usuario", usuario);
            return "usuarios/detalle";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("mensaje", "Usuario no encontrado");
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/web/usuarios";
        }
    }

    // Formulario para editar usuario
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            UsuarioResponse usuario = usuarioService.getUsuarioById(id);
            List<Rol> roles = usuarioService.getAllRoles();

            // Convertir a UsuarioUpdateRequest
            UsuarioUpdateRequest usuarioRequest = new UsuarioUpdateRequest();
            usuarioRequest.setCedula(usuario.getCedula());
            usuarioRequest.setNombre(usuario.getNombre());
            usuarioRequest.setTelefono(usuario.getTelefono());
            usuarioRequest.setDireccion(usuario.getDireccion());
            usuarioRequest.setActivo(usuario.getActivo());

            // Obtener IDs de roles
            Set<Long> rolesIds = usuario.getRoles().stream()
                    .map(rol -> rol.getId())
                    .collect(Collectors.toSet());
            usuarioRequest.setRolesIds(rolesIds);

            model.addAttribute("usuario", usuarioRequest);
            model.addAttribute("usuarioId", id);
            model.addAttribute("usuarioEmail", usuario.getEmail());
            model.addAttribute("roles", roles);
            model.addAttribute("action", "editar");
            return "usuarios/formulario";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("mensaje", "Usuario no encontrado");
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/web/usuarios";
        }
    }

    // Actualizar usuario
    @PostMapping("/actualizar/{id}")
    public String actualizarUsuario(@PathVariable Long id,
                                    @Valid @ModelAttribute("usuario") UsuarioUpdateRequest usuario,
                                    BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            List<Rol> roles = usuarioService.getAllRoles();
            UsuarioResponse usuarioActual = usuarioService.getUsuarioById(id);
            model.addAttribute("usuarioId", id);
            model.addAttribute("usuarioEmail", usuarioActual.getEmail());
            model.addAttribute("roles", roles);
            model.addAttribute("action", "editar");
            return "usuarios/formulario";
        }


        try {
            usuarioService.updateUsuario(id, usuario);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/web/usuarios";
    }

    // En el controlador
    @GetMapping("/cambiar-password/{id}")
    public String mostrarFormularioCambiarPassword(@PathVariable Long id, Model model) {
        model.addAttribute("usuarioId", id);
        model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
        return "usuarios/cambiar-password";
    }

    @PostMapping("/cambiar-password/{id}")
    public String cambiarPassword(@PathVariable Long id,
                                  @Valid @ModelAttribute ChangePasswordRequest request,
                                  BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            // manejar errores
            return "usuarios/cambiar-password";
        }

        try {
            usuarioService.changePassword(id, request);
            redirectAttributes.addFlashAttribute("mensaje", "Contraseña cambiada exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/web/usuarios";
    }
}