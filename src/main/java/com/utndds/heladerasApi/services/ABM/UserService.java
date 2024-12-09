package com.utndds.heladerasApi.services.ABM;

import com.utndds.heladerasApi.DTOs.PersonaHumanaDTO;
import com.utndds.heladerasApi.DTOs.PersonaJuridicaDTO;
import com.utndds.heladerasApi.models.Persona.Documento;
import com.utndds.heladerasApi.models.Persona.Persona;
import com.utndds.heladerasApi.models.Persona.PersonaHumana;
import com.utndds.heladerasApi.repositories.PersonaRepository;
import com.utndds.heladerasApi.models.Persona.PersonaJuridica;
import com.utndds.heladerasApi.models.Persona.Contacto.Contacto;
import com.utndds.heladerasApi.models.Persona.Contacto.Email;
import com.utndds.heladerasApi.models.Persona.Contacto.Telegram;
import com.utndds.heladerasApi.models.Persona.Contacto.Whatsapp;
import com.utndds.heladerasApi.models.Rol.Colaborador;
import java.util.ArrayList;
import java.util.List;
import com.utndds.heladerasApi.repositories.ColaboradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    public Long crearPersonaHumana(PersonaHumanaDTO personaHumanaDTO) {
        validarDTOHumano(personaHumanaDTO);

        // Crear instancia de PersonaHumana sin contactos por ahora
        PersonaHumana personaHumana = new PersonaHumana(
                personaHumanaDTO.getDireccion(),
                new ArrayList<>(), // Lista vacía inicialmente
                personaHumanaDTO.getNombre(),
                personaHumanaDTO.getApellido(),
                personaHumanaDTO.getFechaNacimiento(),
                crearDocumento(personaHumanaDTO.getTipo(), personaHumanaDTO.getDocumento()));

        // Crear contactos y asociarlos con la persona
        List<Contacto> mediosContacto = crearContactos(
                personaHumanaDTO.getMediosContacto(),
                personaHumanaDTO.getEmail(),
                personaHumanaDTO.getWhatsapp(),
                personaHumanaDTO.getTelegram());

        for (Contacto contacto : mediosContacto) {
            contacto.setPersona(personaHumana); // Asociar el contacto con la persona
        }

        // Asignar los contactos a la persona
        personaHumana.setMediosContacto(mediosContacto);

        // Guardar la persona y sus contactos
        personaRepository.save(personaHumana);

        return personaHumana.getId();
    }

    public Long crearPersonaJuridica(PersonaJuridicaDTO personaJuridicaDTO) {
        validarDTOJuridico(personaJuridicaDTO);

        List<Contacto> mediosContacto = crearContactos(
                personaJuridicaDTO.getMediosContacto(),
                personaJuridicaDTO.getEmail(),
                personaJuridicaDTO.getWhatsapp(),
                personaJuridicaDTO.getTelegram());

        PersonaJuridica personaJuridica = new PersonaJuridica(
                personaJuridicaDTO.getDireccion(),
                mediosContacto,
                personaJuridicaDTO.getRazonSocial(),
                personaJuridicaDTO.getTipo(),
                personaJuridicaDTO.getRubro());

        personaRepository.save(personaJuridica);
        return personaJuridica.getId();
    }

    public void crearColaborador(String uuid, String id) {
        try {
            Long personaId = Long.parseLong(id);
            Persona persona = personaRepository.findById(personaId)
                    .orElseThrow(() -> new IllegalArgumentException("La persona no existe."));

            Colaborador colaborador = new Colaborador(persona);
            colaborador.setUUID(uuid);
            colaboradorRepository.save(colaborador);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El ID proporcionado no es válido.", e);
        }
    }

    private List<Contacto> crearContactos(List<String> mediosContacto, String valorEmail, String valorWhatsapp,
            String valorTelegram) {
        List<Contacto> contactos = new ArrayList<>();
        for (String medio : mediosContacto) {
            if (medio != null) {
                switch (medio.toUpperCase()) {
                    case "EMAIL":
                        if (valorEmail != null)
                            contactos.add(new Email(valorEmail));
                        break;
                    case "WHATSAPP":
                        if (valorWhatsapp != null)
                            contactos.add(new Whatsapp(valorWhatsapp));
                        break;
                    case "TELEGRAM":
                        if (valorTelegram != null)
                            contactos.add(new Telegram(valorTelegram));
                        break;
                    default:
                        throw new IllegalArgumentException("Medio de contacto no soportado: " + medio);
                }
            }
        }
        return contactos;
    }


    private Documento crearDocumento(String tipo, String numero) {

        if ((tipo == null || tipo.isEmpty()) && (numero == null || numero.isEmpty())) {
            return null;
        }

        if (numero == null || numero.isEmpty() || !numero.matches("\\d{1,10}")) {
            throw new IllegalArgumentException(
                    "El número de documento debe ser válido y contener entre 1 y 10 dígitos.");
        }
        if (tipo == null && numero != null) {
            return null;
        }
        if (tipo != null && numero == null) {
            return null;
        }

        return new Documento(tipo.toUpperCase(), numero);
    }

    private void validarDTOHumano(PersonaHumanaDTO dto) {
        if (dto.getNombre() == null || dto.getNombre().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        if (dto.getApellido() == null || dto.getApellido().isEmpty()) {
            throw new IllegalArgumentException("El apellido no puede estar vacío.");
        }
    }

    private void validarDTOJuridico(PersonaJuridicaDTO dto) {
        if (dto.getRazonSocial() == null || dto.getRazonSocial().isEmpty()) {
            throw new IllegalArgumentException("La razón social no puede estar vacía.");
        }
    }
}
