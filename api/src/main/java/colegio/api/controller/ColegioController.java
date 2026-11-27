package colegio.api.controller;

import colegio.api.model.SolicitudEntity;
import colegio.api.model.UsuarioEntity;
import colegio.api.repository.SolicitudRepository;
import colegio.api.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ColegioController {

    private final UsuarioRepository usuarioRepo;
    private final SolicitudRepository solicitudRepo;

    public ColegioController(UsuarioRepository usuarioRepo, SolicitudRepository solicitudRepo) {
        this.usuarioRepo = usuarioRepo;
        this.solicitudRepo = solicitudRepo;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("estado", "ok");
    }

    @GetMapping("/usuarios")
    public List<UsuarioEntity> listarUsuarios() {
        return usuarioRepo.findAll();
    }

    @PostMapping("/usuarios")
    public UsuarioEntity crearUsuario(@RequestBody UsuarioEntity usuario) {
        return usuarioRepo.save(usuario);
    }

    @PutMapping("/usuarios/{correo}/aprobar")
    public ResponseEntity<UsuarioEntity> aprobarUsuario(@PathVariable String correo) {
        return usuarioRepo.findById(correo).map(u -> {
            u.setAprobado(true);
            return ResponseEntity.ok(usuarioRepo.save(u));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/usuarios/{correo}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable String correo) {
        if (!usuarioRepo.existsById(correo)) {
            return ResponseEntity.notFound().build();
        }
        usuarioRepo.deleteById(correo);
        return ResponseEntity.noContent().build();
    }

    private void actualizarReservasVencidas() {
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Santiago"));
        List<SolicitudEntity> todas = solicitudRepo.findAll();

        for (SolicitudEntity s : todas) {
            String f = s.getFechaReserva();
            if (f != null && f.matches("\\d{4}-\\d{2}-\\d{2}")) {
                LocalDate fecha = LocalDate.parse(f);
                if (fecha.isBefore(hoy)) {
                    if ("APROBADA".equals(s.getEstado())) {
                        s.setEstado("FINALIZADA");
                        solicitudRepo.save(s);
                    } else if ("PENDIENTE".equals(s.getEstado())) {
                        s.setEstado("RECHAZADA");
                        solicitudRepo.save(s);
                    }
                }
            }
        }
    }

    @GetMapping("/solicitudes")
    public List<SolicitudEntity> listarSolicitudes() {
        actualizarReservasVencidas();
        return solicitudRepo.findAll();
    }

    @PostMapping("/solicitudes")
    public SolicitudEntity crearSolicitud(@RequestBody SolicitudEntity solicitud) {
        solicitud.setId(null);
        return solicitudRepo.save(solicitud);
    }

    @PutMapping("/solicitudes/{id}/estado")
    public ResponseEntity<SolicitudEntity> cambiarEstado(
            @PathVariable Integer id,
            @RequestParam String estado
    ) {
        return solicitudRepo.findById(id).map(sol -> {
            sol.setEstado(estado);
            SolicitudEntity guardada = solicitudRepo.save(sol);

            if ("APROBADA".equals(estado)) {
                for (SolicitudEntity otra : solicitudRepo.findAll()) {
                    if (!otra.getId().equals(id)
                            && otra.getPuestoId() == sol.getPuestoId()
                            && otra.getModuloId() == sol.getModuloId()
                            && otra.getFechaReserva().equals(sol.getFechaReserva())
                            && "PENDIENTE".equals(otra.getEstado())) {
                        otra.setEstado("RECHAZADA");
                        solicitudRepo.save(otra);
                    }
                }
            }
            return ResponseEntity.ok(guardada);
        }).orElse(ResponseEntity.notFound().build());
    }
}