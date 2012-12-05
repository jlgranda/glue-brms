/**
 * This file is part of Glue: Adhesive BRMS
 * 
* Copyright (c)2012 José Luis Granda <jlgranda@eqaula.org> (Eqaula Tecnologías
 * Cia Ltda) Copyright (c)2012 Eqaula Tecnologías Cia Ltda (http://eqaula.org)
 * 
* If you are developing and distributing open source applications under the GNU
 * General Public License (GPL), then you are free to re-distribute Glue under
 * the terms of the GPL, as follows:
 * 
* GLue is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
* Glue is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
* You should have received a copy of the GNU General Public License along with
 * Glue. If not, see <http://www.gnu.org/licenses/>.
 * 
* For individuals or entities who wish to use Glue privately, or internally,
 * the following terms do not apply:
 * 
* For OEMs, ISVs, and VARs who wish to distribute Glue with their products, or
 * host their product online, Eqaula provides flexible OEM commercial licenses.
 * 
* Optionally, Customers may choose a Commercial License. For additional
 * details, contact an Eqaula representative (sales@eqaula.org)
 */
package org.eqaula.glue.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.TypedQuery;
import org.eqaula.glue.model.BussinesEntityType;
import org.eqaula.glue.model.Group;
import org.eqaula.glue.model.Structure;
import org.eqaula.glue.model.Property;
import org.eqaula.glue.model.config.Setting;
import org.eqaula.glue.model.profile.Profile;
import org.eqaula.glue.model.security.IdentityObjectCredentialType;
import org.eqaula.glue.model.security.IdentityObjectType;
import org.eqaula.glue.service.BussinesEntityService;
import org.eqaula.glue.util.Dates;
import org.jboss.seam.security.management.picketlink.IdentitySessionProducer;

import org.jboss.seam.transaction.TransactionPropagation;
import org.jboss.seam.transaction.Transactional;
import org.jboss.solder.servlet.WebApplication;
import org.jboss.solder.servlet.event.Initialized;
import org.picketlink.idm.api.IdentitySession;
import org.picketlink.idm.api.IdentitySessionFactory;
import org.picketlink.idm.api.User;
import org.picketlink.idm.common.exception.IdentityException;

/**
 * Validates that the database contains the minimum required entities to
 * function from SocialPM
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @adapter <a href="mailto:jlgranda81@gmail.com">José Luis Granda</a>
 */
@Transactional(TransactionPropagation.REQUIRED)
public class InitializeDatabase {

    @PersistenceContext(type = PersistenceContextType.EXTENDED)
    private EntityManager entityManager;
    @Inject
    private IdentitySessionFactory identitySessionFactory;
    @Inject
    protected BussinesEntityService bussinesEntityService;

    @Transactional
    public void validate(@Observes @Initialized final WebApplication webapp) throws IdentityException {
        bussinesEntityService.setEntityManager(entityManager);
        validateDB();
        validateStructure();
        validateIdentityObjectTypes();
        validateSecurity();

    }

    private void validateDB() {
        Setting singleResult = null;
        try {
            TypedQuery<Setting> query = entityManager.createQuery("from Setting s where s.name='schemaVersion'",
                    Setting.class);
            singleResult = query.getSingleResult();
        } catch (NoResultException e) {
            Date now = Calendar.getInstance().getTime();
            singleResult = new Setting("schemaVersion", "0");
            singleResult.setCreatedOn(now);
            singleResult.setLastUpdate(now);
            entityManager.persist(singleResult);
            entityManager.flush();
        }

        System.out.println("Current database schema version is [" + singleResult.getValue() + "]");

    }

    private void validateIdentityObjectTypes() {
        if (entityManager.createQuery("select t from IdentityObjectType t where t.name = :name")
                .setParameter("name", "USER")
                .getResultList().size() == 0) {

            IdentityObjectType user = new IdentityObjectType();
            user.setName("USER");
            entityManager.persist(user);
        }

        if (entityManager.createQuery("select t from IdentityObjectType t where t.name = :name")
                .setParameter("name", "GROUP")
                .getResultList().size() == 0) {

            IdentityObjectType group = new IdentityObjectType();
            group.setName("GROUP");
            entityManager.persist(group);
        }
    }

    private void validateSecurity() throws IdentityException {
        // Validate credential types
        if (entityManager.createQuery("select t from IdentityObjectCredentialType t where t.name = :name")
                .setParameter("name", "PASSWORD")
                .getResultList().size() == 0) {

            IdentityObjectCredentialType PASSWORD = new IdentityObjectCredentialType();
            PASSWORD.setName("PASSWORD");
            entityManager.persist(PASSWORD);
        }

        Map<String, Object> sessionOptions = new HashMap<String, Object>();
        sessionOptions.put(IdentitySessionProducer.SESSION_OPTION_ENTITY_MANAGER, entityManager);


        IdentitySession session = identitySessionFactory.createIdentitySession("default", sessionOptions);
        /*
         * Create our test user (me!)
         */
        Date now = Calendar.getInstance().getTime();
        BussinesEntityType bussinesEntityType = null;
        TypedQuery<BussinesEntityType> query = entityManager.createQuery("from BussinesEntityType b where b.name=:name",
                BussinesEntityType.class);
        query.setParameter("name", Profile.class.getName());
        bussinesEntityType = query.getSingleResult();
        if (session.getPersistenceManager().findUser("jlgranda") == null) {
            User u = session.getPersistenceManager().createUser("jlgranda");
            session.getAttributesManager().updatePassword(u, "password");
            session.getAttributesManager().addAttribute(u, "email", "jlgranda81@gmail.com");




            Profile p = new Profile();
            p.setEmail("jlgranda81@gmail.com");
            p.setUsername("jlgranda");
            p.setPassword("password");
            p.getIdentityKeys().add(u.getKey());
            p.setUsernameConfirmed(true);
            p.setShowBootcamp(true);

            p.setName("José Luis");
            p.setFirstname("José Luis");
            p.setSurname("Granda");
            p.setCreatedOn(now);
            p.setLastUpdate(now);
            p.setActivationTime(now);
            p.setExpirationTime(Dates.addDays(now, 364));
            p.setAuthor(null); //Establecer al usuario actual
            p.setType(bussinesEntityType); //Relacionar con un tipo de entidad de negocio y su estructura
            p.buildAttributes(bussinesEntityService); //Crear la estructura de datos glue
            entityManager.persist(p);
            entityManager.flush();

        }

        /*
         * Create test user (kenfinnigan)
         *//*
        if (session.getPersistenceManager().findUser("rcmacas") == null) {
            User u = session.getPersistenceManager().createUser("rcmacas");
            session.getAttributesManager().updatePassword(u, "password");
            session.getAttributesManager().addAttribute(u, "email", "rcmacas@gmail.com");

            Profile p = new Profile();
            p.setEmail("rcmacas@gmail.com");
            p.setUsername("rcmacas");
            p.setPassword("password");
            p.getIdentityKeys().add(u.getKey());
            p.setUsernameConfirmed(true);
            p.setShowBootcamp(true);

            p.setName("");
            p.setFirstname("Rita Cecibe");
            p.setSurname("Macas Gonzalez");
            p.setCreatedOn(now);
            p.setLastUpdate(now);
            p.setActivationTime(now);
            p.setExpirationTime(Dates.addDays(now, 364));
            p.setAuthor(null); //Establecer al usuario actual
            p.setType(bussinesEntityType);
            p.buildAttributes(bussinesEntityService); //Crear la estructura de datos glue
            entityManager.persist(p);
            entityManager.flush();
        }*/

        /*
         * Create test user (bleathem)
         */
        //      if (session.getPersistenceManager().findUser("bleathem") == null) {
        //         User u = session.getPersistenceManager().createUser("bleathem");
        //         session.getAttributesManager().updatePassword(u, "password");
        //         session.getAttributesManager().addAttribute(u, "email", "bleathem@gmail.com");
        //
        //         Profile p = new Profile();
        //         p.setEmail("bleathem@gmail.com");
        //         p.setUsername("bleathem");
        //         p.getIdentityKeys().add(u.getKey());
        //         p.setUsernameConfirmed(true);
        //         p.setShowBootcamp(true);
        //         entityManager.persist(p);
        //         entityManager.flush();
        //      }

    }

    private void validateStructure() {
        validateStructureForProfile();
        validateStructureForPersonalData();
        validateStructureForSpouse();
        validateStructureForChildrens();
        validateStructureForStrategic();
        validateStructureForEducation();
        validateStructureForRole();
        validateStructureForTasks();
        validateStructureForCapacitacion();
        validateStructureForTrayectoriaLaboral();
    }

    private void validateStructureForProfile() {
        BussinesEntityType bussinesEntityType = null; 

        try {
            TypedQuery<BussinesEntityType> query = entityManager.createQuery("from BussinesEntityType b where b.name=:name",
                    BussinesEntityType.class);
            query.setParameter("name", Profile.class.getName());
            bussinesEntityType = query.getSingleResult();
        } catch (NoResultException e) {
            bussinesEntityType = new BussinesEntityType();
            bussinesEntityType.setName(Profile.class.getName());

            //Agrupaciones de propiedades
            Date now = Calendar.getInstance().getTime();
            Calendar ago = Calendar.getInstance();
            ago.add(Calendar.DAY_OF_YEAR, (-1 * 364 * 18)); //18 años atras
            Structure structure = null;
            structure = new Structure();
            structure.setName("Data for " + Profile.class.getName());
            structure.setCreatedOn(now);
            structure.setLastUpdate(now);

            //Lista de atributos de entidad de negocios
            List<Property> attributes = new ArrayList<Property>();

            
            attributes.add(buildAttribute("PersonalData", Structure.class.getName(), null, true, "Datos personales", "Información personal relevante", null, "/pages/profile/data/personal"));
            attributes.add(buildAttribute("Spouse", Group.class.getName(), null, true, "Esposa/o", "Datos de su conyugue"));
            attributes.add(buildAttribute("Childrens", Group.class.getName(), null, true, "Hijos", "Datos de sus hijos"));
            attributes.add(buildAttribute("Education", Group.class.getName(), null, true, "Educación", "Detalle sus logros académicos"));
            attributes.add(buildAttribute("Role", Structure.class.getName(), null, true, "Detalle del cargo que desempeña", "Detalle del cargo que desempeña"));
            attributes.add(buildAttribute("Strategic", Structure.class.getName(), null, true, "Datos estratégicos", "Custionario para recolectar información estratégica"));
            attributes.add(buildAttribute("Tasks", Group.class.getName(), null, true, "Tareas a su cargo", "Detalle las tareas que corresponden a su cargo dentro de la institución"));
            attributes.add(buildAttribute("Capacitacion", Group.class.getName(), null, true, "Eventos de Capacitación", "Detalle de capacitaciones con  respaldo fisico notarizado desde el año 2000 en adelante"));
            attributes.add(buildAttribute("TrayectoriaLaboral", Group.class.getName(), null, true, "Trayectoria Laboral", "Detalle de la trayectoria laboral desde el año 2000 en adelante"));


            //Agregar atributos
            structure.setProperties(attributes);

            bussinesEntityType.addStructure(structure);

            entityManager.persist(bussinesEntityType);
            entityManager.flush();
        }

        System.out.println("Structure for Profile [" + bussinesEntityType + "]");
    }

    private void validateStructureForPersonalData() {
        BussinesEntityType bussinesEntityType = null;
        String name = "PersonalData";
        try {
            TypedQuery<BussinesEntityType> query = entityManager.createQuery("from BussinesEntityType b where b.name=:name",
                    BussinesEntityType.class);
            query.setParameter("name", name);
            bussinesEntityType = query.getSingleResult();
        } catch (NoResultException e) {
            bussinesEntityType = new BussinesEntityType();
            bussinesEntityType.setName(name);

            //Agrupaciones de propiedades
            Date now = Calendar.getInstance().getTime();
            Calendar ago = Calendar.getInstance();
            ago.add(Calendar.DAY_OF_YEAR, (-1 * 364 * 18)); //18 años atras
            Structure structure = null;
            structure = new Structure();
            structure.setName("Data for " + name);
            structure.setCreatedOn(now);
            structure.setLastUpdate(now);

            //Lista de atributos de entidad de negocios
            List<Property> attributes = new ArrayList<Property>();

            /*
             * Personal
             ESTADO CIVIL	FECHA DE NACIMIENTO (en números) día/mes/año	GENERO	NACIONALIDAD	MODALIDAD: CONTRATO O NOMBRAMIENTO	LIBRETA  MILITAR           (sin guión)	TIPO DE SANGRE	
             * Discapacidad
             * Discapacidad     SI   /   NO	N° CONADIS (sin guión)       / SIN  TIENE CARNET	TIPO FÍSICA/MENTAL	% PORCENTAJE	
             * Dirección permanente
             * CALLE PRINCIPAL	No.	CALLE SECUNDARIA	REFERENCIA (SECTOR)	PROVINCIA	PARROQUIA	CANTON	TELÉFONO DOMICILIO SIN GUIÓN 	TELÉFONO CELULAR  SIN GUIÓN 	TELÉFONO DE TRABAJO  SIN GUIÓN     	CORREO ELECTRÓNICO  (INSTITUCIONAL/ LABORAL)	CORREO ELECTRÓNICO (PERSONAL)	
             * Contacto en emergencia
             * APELLIDOS	NOMBRES	TELÉFONO LOCAL      SIN GUIÓN 	TELEFONO CELULAR  SIN GUIÓN 	
             * DEclaración de bienes
             * No. DE NOTARIA DE DECLARACIÓN DE BIENES	LUGAR DE NOTARIA DE DECLARACION DE BIENES      (CIUDAD - CANTÓN)	FECHA DE DECLARACION DE BIENES            (en números) día/mes/año
             * AUTOIDENTIFICACION ETNICA	
             * AFROECUATORIANO/AFRODESCENDIENTE - BLANCO - INDIGENA - MESTIZO - MONTUBIO - MULATO - NEGRO - OTRA	
             * EN CASO DE SELECCIONAR INDIGENA:   ACHUAR - ANDOA - AWA - CHACHI - COFAN - EPERA - KICHWA - SECOYA - SHIWIAR - SHUAR - SIONA - TSACHILA - WAORANI - ZAPARA
             */
            attributes.add(buildAttribute("Personal", "maritalstatus", "java.lang.String[]", "Casado,Soltero,Divorciado,Unión libre", false, "Estado civil", "Indique su estado civil"));
            attributes.add(buildAttribute("Personal", "birthday", Date.class.getName(), ago.getTime(), false, "Fecha de nacimiento", "Nunca olvidaremos su cumpleaños"));
            attributes.add(buildAttribute("Personal", "gender", "java.lang.String[]", "Másculino,Femenino", false, "Género", ""));
            attributes.add(buildAttribute("Personal", "birthplace", String.class.getName(), "Loja", false, "Lugar de nacimiento", "Dónde nacio?"));
            attributes.add(buildAttribute("Dirección permanente", "country", String.class.getName(), "Ecuador", false, "País", "País de residencia"));
            attributes.add(buildAttribute("Dirección permanente", "city", String.class.getName(), "Loja", false, "Ciudad", "Ciudad de residencia"));
            attributes.add(buildAttribute("Dirección permanente", "parish", String.class.getName(), null, false, "Parroquia", "Parroquia de residencia"));
            attributes.add(buildAttribute("Dirección permanente", "neighborhood", String.class.getName(), null, false, "Barrio", "Barrio de residencia"));
            attributes.add(buildAttribute("Dirección permanente", "address", String.class.getName(), null, false, "Dirección", "Calles y número de casa"));
            attributes.add(buildAttribute("Dirección permanente", "phone", String.class.getName(), null, false, "Teléfono", "Telefóno de contacto"));
            attributes.add(buildAttribute("Contacto en emergencia", "emergencyContact", String.class.getName(), null, false, "Contacta en caso de emergencia", "Sí se presenta alguna emergencia, a quién debemos llamar?"));
            attributes.add(buildAttribute("hobies", "java.lang.MultiLineString", null, false, "Hobies", "Las cosas que disfruta en su tiempo libre (separe con comas)"));

            //Agregar atributos
            structure.setProperties(attributes);

            bussinesEntityType.addStructure(structure);

            entityManager.persist(bussinesEntityType);
            entityManager.flush();
        }

    }

    private void validateStructureForSpouse() {
        BussinesEntityType bussinesEntityType = null;
        String name = "Spouse";
        try {
            TypedQuery<BussinesEntityType> query = entityManager.createQuery("from BussinesEntityType b where b.name=:name",
                    BussinesEntityType.class);
            query.setParameter("name", name);
            bussinesEntityType = query.getSingleResult();
        } catch (NoResultException e) {
            bussinesEntityType = new BussinesEntityType();
            bussinesEntityType.setName(name);

            //Agrupaciones de propiedades
            Date now = Calendar.getInstance().getTime();
            Calendar ago = Calendar.getInstance();
            ago.add(Calendar.DAY_OF_YEAR, (-1 * 364 * 18)); //18 años atras
            Structure structure = null;
            structure = new Structure();
            structure.setName("Data for " + name);
            structure.setCreatedOn(now);
            structure.setLastUpdate(now);

            //Lista de atributos de entidad de negocios
            List<Property> attributes = new ArrayList<Property>();

            //attributes.add(buildAttribute("birthday", Date.class.getName(), ago.getTime(), true, "Fecha de nacimiento", "Nunca olvidaremos su cumpleaños"));
            attributes.add(buildAttribute("tipoRelacion", "java.lang.String[]","Casado,Unión libre", true, "Tipo de Relacion", "Indique que relacion tiene"));             
            attributes.add(buildAttribute("empresaTrabajo", String.class.getName(), null, false, "Empresa en la que Trabaja", "Indique la empresa que trabaja"));
            attributes.add(buildAttribute("funcionCargo", String.class.getName(), null ,false, "Actividad / Función o Cargo", "Que cargo o funcion tiene"));

            //Agregar atributos
            structure.setProperties(attributes);

            bussinesEntityType.addStructure(structure);

            entityManager.persist(bussinesEntityType);
            entityManager.flush();
        }

    }

    private void validateStructureForChildrens() {
        BussinesEntityType bussinesEntityType = null;
        String name = "Childrens";
        try {
            TypedQuery<BussinesEntityType> query = entityManager.createQuery("from BussinesEntityType b where b.name=:name",
                    BussinesEntityType.class);
            query.setParameter("name", name);
            bussinesEntityType = query.getSingleResult();
        } catch (NoResultException e) {
            bussinesEntityType = new BussinesEntityType();
            bussinesEntityType.setName(name);

            //Agrupaciones de propiedades
            Date now = Calendar.getInstance().getTime();
            Calendar ago = Calendar.getInstance();
            ago.add(Calendar.DAY_OF_YEAR, (-1 * 364 * 18)); //18 años atras
            Structure structure = null;
            structure = new Structure();
            structure.setName("Data for " + name);
            structure.setCreatedOn(now);
            structure.setLastUpdate(now);

            //Lista de atributos de entidad de negocios
            List<Property> attributes = new ArrayList<Property>();

            //attributes.add(buildAttribute("birthday", Date.class.getName(), ago.getTime(), true, "Fecha de nacimiento", "Nunca olvidaremos su cumpleaños"));
            attributes.add(buildAttribute("birthday", Date.class.getName(), ago.getTime(), false, "Fecha de nacimiento", "Nunca olvidaremos su cumpleaños"));
            attributes.add(buildAttribute("tipoInstruccion", "java.lang.String[]","Pre-Basica,Basica,Bachillerato,Egresado,Superior,4 Nivel,Ninguno", false, "Tipo de Instrucción", "Indique el tipo de instrucción"));
            
            //Agregar atributos
            structure.setProperties(attributes);

            bussinesEntityType.addStructure(structure);

            entityManager.persist(bussinesEntityType);
            entityManager.flush();
        }

    }

    private void validateStructureForEducation() {
        BussinesEntityType bussinesEntityType = null;
        String name = "Education";
        try {
            TypedQuery<BussinesEntityType> query = entityManager.createQuery("from BussinesEntityType b where b.name=:name",
                    BussinesEntityType.class);
            query.setParameter("name", name);
            bussinesEntityType = query.getSingleResult();
        } catch (NoResultException e) {
            bussinesEntityType = new BussinesEntityType();
            bussinesEntityType.setName(name);

            //Agrupaciones de propiedades
            Date now = Calendar.getInstance().getTime();
            Calendar ago = Calendar.getInstance();
            ago.add(Calendar.DAY_OF_YEAR, (-1 * 364 * 18)); //18 años atras
            Structure structure = null;
            structure = new Structure();
            structure.setName("Data for " + name);
            structure.setCreatedOn(now);
            structure.setLastUpdate(now);

            //Lista de atributos de entidad de negocios
            List<Property> attributes = new ArrayList<Property>();

            attributes.add(buildAttribute("title", String.class.getName(), "", true, "Titulo", "Qué titulación obtuviste?"));
            attributes.add(buildAttribute("country", String.class.getName(), "", true, "País", ""));
            attributes.add(buildAttribute("institution", String.class.getName(), "", true, "Institución", "Centro de estudios"));
            attributes.add(buildAttribute("start", Date.class.getName(), now, true, "Fecha de inicio", "Cuándo inicio?"));
            attributes.add(buildAttribute("end", Date.class.getName(), now, false, "Fecha de fin", "Cuándo finalizó?"));
            attributes.add(buildAttribute("atPresent", Boolean.class.getName(), false, true, "Al presente", ""));
            attributes.add(buildAttribute("level", "java.lang.String[]", "Secundario,Terciario,Universitario,Postgrado,Master,Doctorado,Otro", true, "Nivel de estudio", "Nivel de los estudios cursados"));
//            attributes.add(buildAttribute("status", "java.lang.String[]", "Graduado,En curso,Abandonado", true, "Nivel de estudio", "Nivel de los estudios cursados"));

            //Agregar atributos
            structure.setProperties(attributes);

            bussinesEntityType.addStructure(structure);

            entityManager.persist(bussinesEntityType);
            entityManager.flush();
        }
    }

    private void validateStructureForRole() {
        BussinesEntityType bussinesEntityType = null;
        String name = "Role";
        try {
            TypedQuery<BussinesEntityType> query = entityManager.createQuery("from BussinesEntityType b where b.name=:name",
                    BussinesEntityType.class);
            query.setParameter("name", name);
            bussinesEntityType = query.getSingleResult();
        } catch (NoResultException e) {
            bussinesEntityType = new BussinesEntityType();
            bussinesEntityType.setName(name);

            //Agrupaciones de propiedades
            Date now = Calendar.getInstance().getTime();
            Calendar ago = Calendar.getInstance();
            ago.add(Calendar.DAY_OF_YEAR, (-1 * 364 * 18)); //18 años atras
            Structure structure = null;
            structure = new Structure();
            structure.setName("Data for " + name);
            structure.setCreatedOn(now);
            structure.setLastUpdate(now);

            //Lista de atributos de entidad de negocios
            List<Property> attributes = new ArrayList<Property>();

            attributes.add(buildAttribute("role", String.class.getName(), "", true, "Cargo actual", "El cargo actual que desempeña"));
            attributes.add(buildAttribute("remuneracion", Double.class.getName(), "", true, "Remuneración unificada", "Monto de remuneración unificada del cargo"));
            attributes.add(buildAttribute("jefe", String.class.getName(), "", true, "Jefe inmediato", "El nombre de su jefe inmediato"));
            attributes.add(buildAttribute("tipoContratacion", String.class.getName(), "", true, "Tipo de contrato", "La modalidad de contratación"));
            attributes.add(buildAttribute("tituloProfesional1", String.class.getName(), "", true, "Título profesional", "Indique el título profesional que posee"));
            attributes.add(buildAttribute("tituloProfesional2", String.class.getName(), "", false, "Otro título profesional", "Indique el título profesional que posee"));
            attributes.add(buildAttribute("tituloCuartoNivel1", String.class.getName(), "", false, "Título de cuarto nivel", "Indique el título de cuarto nivel que posee"));
            attributes.add(buildAttribute("tituloCuartoNivel2", String.class.getName(), "", false, "Otro título de cuarto nivel", "Indique el título de cuarto nivel que posee"));
            attributes.add(buildAttribute("aniosServicio", Long.class.getName(), "", true, "Años de servicio", "Indique el tiempo en años que va laborando"));
            attributes.add(buildAttribute("historiaLaboral", String.class.getName(), "", true, "Historia laboral", "Indique el código de historia laboral"));
            attributes.add(buildAttribute("logrosPersonales", String.class.getName(), "", false, "Logros personales relevantes", "Indique los logros personales relavantes en relación a la institución"));
            attributes.add(buildAttribute("comentario", "java.lang.MultiLineString", "", false, "Comentario", "Haganos conocer sus ideas y/o comentarios acerca de este proceso"));

            //Agregar atributos
            structure.setProperties(attributes);

            bussinesEntityType.addStructure(structure);

            entityManager.persist(bussinesEntityType);
            entityManager.flush();
        }
    }

    private void validateStructureForStrategic() {
        BussinesEntityType bussinesEntityType = null;
        String name = "Strategic";
        try {
            TypedQuery<BussinesEntityType> query = entityManager.createQuery("from BussinesEntityType b where b.name=:name",
                    BussinesEntityType.class);
            query.setParameter("name", name);
            bussinesEntityType = query.getSingleResult();
        } catch (NoResultException e) {
            bussinesEntityType = new BussinesEntityType();
            bussinesEntityType.setName(name);

            //Agrupaciones de propiedades
            Date now = Calendar.getInstance().getTime();
            Calendar ago = Calendar.getInstance();
            ago.add(Calendar.DAY_OF_YEAR, (-1 * 364 * 18)); //18 años atras
            Structure structure = null;
            structure = new Structure();
            structure.setName("Data for " + name);
            structure.setCreatedOn(now);
            structure.setLastUpdate(now);

            //Lista de atributos de entidad de negocios
            List<Property> attributes = new ArrayList<Property>();

            attributes.add(buildAttribute("mision", "java.lang.MultiLineString", "", true, "Cuál cree usted que debería ser la misión de la empresa pública?", "Analise la pregunta y conteste en el espacio correspondiente"));
            attributes.add(buildAttribute("vision", "java.lang.MultiLineString", "", true, "Cuál cree usted que debería ser la visión de la empresa pública?", "Analise la pregunta y conteste en el espacio correspondiente"));
            attributes.add(buildAttribute("objetivos", "java.lang.MultiLineString", "", true, "Cuál cree usted que debería ser los objetivos estratégicos de la empresa pública?", "Analise la pregunta y conteste en el espacio correspondiente"));
            attributes.add(buildAttribute("fortalezas", "java.lang.MultiLineString", "", true, "Identifique fortalezas para la nueva empresa?", "Analise la pregunta y conteste en el espacio correspondiente. Utilice comas para separar varias."));
            attributes.add(buildAttribute("oportunidades", "java.lang.MultiLineString", "", true, "Identifique oportunidades para la nueva empresa?", "Analise la pregunta y conteste en el espacio correspondiente. Utilice comas para separar varias."));
            attributes.add(buildAttribute("debilidades", "java.lang.MultiLineString", "", true, "Identifique debilidades para la nueva empresa?", "Analise la pregunta y conteste en el espacio correspondiente. Utilice comas para separar varias."));
            attributes.add(buildAttribute("amenazas", "java.lang.MultiLineString", "", true, "Identifique amanezas para la nueva empresa?", "Analise la pregunta y conteste en el espacio correspondiente. Utilice comas para separar varias."));
            attributes.add(buildAttribute("empresavsunidad", "java.lang.MultiLineString", "", true, "Identifique oportunidades para la nueva empresa?", "Analise la pregunta y conteste en el espacio correspondiente. Utilice comas para separar varias."));
            attributes.add(buildAttribute("ventajas", "java.lang.MultiLineString", "", true, "Menciones ventajas de la empresa en relación a una unidad?", "Analise la pregunta y conteste en el espacio correspondiente. Utilice comas para separar varias."));
            attributes.add(buildAttribute("desventajas", "java.lang.MultiLineString", "", true, "Menciones desventajas de la empresa en relación a una unidad?", "Analise la pregunta y conteste en el espacio correspondiente. Utilice comas para separar varias."));
            attributes.add(buildAttribute("calidad", "java.lang.MultiLineString", "", true, "Ha evidenciado control de calidad en el ejercicio de su gestión?", "Analise la pregunta y conteste en el espacio correspondiente"));
            attributes.add(buildAttribute("aporte", "java.lang.MultiLineString", "", true, "Cómo aportaría ud para la construcción de la nueva empresa pública?", "Analise la pregunta y conteste en el espacio correspondiente"));
            attributes.add(buildAttribute("compromiso", "java.lang.MultiLineString", "", true, "Cuál sería su comprimiso personal para con la nueva empresa pública?", "Analise la pregunta y conteste en el espacio correspondiente"));
            attributes.add(buildAttribute("valoracionservicio", "java.lang.MultiLineString", "", true, "Cómo cree ud que los usuarios valoran el servicio de la UMAPAL?", "Analise la pregunta y conteste en el espacio correspondiente"));
            
            structure.setProperties(attributes);

            bussinesEntityType.addStructure(structure);

            entityManager.persist(bussinesEntityType);
            entityManager.flush();
        }
    }

    private void validateStructureForTasks() {
        BussinesEntityType bussinesEntityType = null;
        String name = "Tasks";
        try {
            TypedQuery<BussinesEntityType> query = entityManager.createQuery("from BussinesEntityType b where b.name=:name",
                    BussinesEntityType.class);
            query.setParameter("name", name);
            bussinesEntityType = query.getSingleResult();
        } catch (NoResultException e) {
            bussinesEntityType = new BussinesEntityType();
            bussinesEntityType.setName(name);

            //Agrupaciones de propiedades
            Date now = Calendar.getInstance().getTime();
            Calendar ago = Calendar.getInstance();
            ago.add(Calendar.DAY_OF_YEAR, (-1 * 364 * 18)); //18 años atras
            Structure structure = null;
            structure = new Structure();
            structure.setName("Data for " + name);
            structure.setCreatedOn(now);
            structure.setLastUpdate(now);

            //Lista de atributos de entidad de negocios
            List<Property> properties = new ArrayList<Property>();

            //TODO crear estructura para registro de tareas


            //Agregar atributos
            structure.setProperties(properties);

            bussinesEntityType.addStructure(structure);

            entityManager.persist(bussinesEntityType);
            entityManager.flush();
        }
    }

    private void validateStructureForCapacitacion() {
      BussinesEntityType bussinesEntityType = null;
        String name = "Capacitacion";
        try {
            TypedQuery<BussinesEntityType> query = entityManager.createQuery("from BussinesEntityType b where b.name=:name",
                    BussinesEntityType.class);
            query.setParameter("name", name);
            bussinesEntityType = query.getSingleResult();
        } catch (NoResultException e) {
            bussinesEntityType = new BussinesEntityType();
            bussinesEntityType.setName(name);

            //Agrupaciones de propiedades
            Date now = Calendar.getInstance().getTime();
            Calendar ago = Calendar.getInstance();
            ago.add(Calendar.DAY_OF_YEAR, (-1 * 364 * 18)); //18 años atras
            Structure structure = null;
            structure = new Structure();
            structure.setName("Data for " + name);
            structure.setCreatedOn(now);
            structure.setLastUpdate(now);

            //Lista de atributos de entidad de negocios
            List<Property> attributes = new ArrayList<Property>();

            //attributes.add(buildAttribute("birthday", Date.class.getName(), ago.getTime(), true, "Fecha de nacimiento", "Nunca olvidaremos su cumpleaños"));
            attributes.add(buildAttribute("nameEvent", String.class.getName(), null ,true, "Nombre del Evento", "Indique el evento que se realizo"));
            attributes.add(buildAttribute("typeEvent", "java.lang.String[]","Curso,Seminario,Taller,Conferencia,Diplomado", true, "Tipo de Evento", "Indique que tipo de evento se realizó"));                     
            attributes.add(buildAttribute("auspiciante", String.class.getName(), null, false, "Auxpiciante", "Indiqueel auxpiciante ya sea Empresa o Institución"));
            attributes.add(buildAttribute("duracionHoras", Long.class.getName(), null, true, "Duración en Horas", "Indique cuantas horas duro el Evento"));
            attributes.add(buildAttribute("country", String.class.getName(), null, false, "País", "País donde se realizo el evento"));
            attributes.add(buildAttribute("dateBegin", Date.class.getName(), ago.getTime(), false, "Fecha de Inicio", "Fecha que inicio el evento"));
            attributes.add(buildAttribute("dateEnd", Date.class.getName(), ago.getTime(), false, "Fecha de Finalización", "Fecha de Culminación"));
            
            //Agregar atributos
            structure.setProperties(attributes);

            bussinesEntityType.addStructure(structure);

            entityManager.persist(bussinesEntityType);
            entityManager.flush();
        }
    }
    
    private void validateStructureForTrayectoriaLaboral() {
      BussinesEntityType bussinesEntityType = null;
        String name = "TrayectoriaLaboral";
        try {
            TypedQuery<BussinesEntityType> query = entityManager.createQuery("from BussinesEntityType b where b.name=:name",
                    BussinesEntityType.class);
            query.setParameter("name", name);
            bussinesEntityType = query.getSingleResult();
        } catch (NoResultException e) {
            bussinesEntityType = new BussinesEntityType();
            bussinesEntityType.setName(name);

            //Agrupaciones de propiedades
            Date now = Calendar.getInstance().getTime();
            Calendar ago = Calendar.getInstance();
            ago.add(Calendar.DAY_OF_YEAR, (-1 * 364 * 18)); //18 años atras
            Structure structure = null;
            structure = new Structure();
            structure.setName("Data for " + name);
            structure.setCreatedOn(now);
            structure.setLastUpdate(now);

            //Lista de atributos de entidad de negocios
            List<Property> attributes = new ArrayList<Property>();
            attributes.add(buildAttribute("tipoInstitucion", "java.lang.String[]","Publica, Privada, Mixta, Otra", true, "Tipo de Institución", "Indique que tipo de institución"));                        
            attributes.add(buildAttribute("nombreInstitucion", String.class.getName(), null ,true, "Nombre de la Institucion", "Indique el nombre de la institución u organización"));
            attributes.add(buildAttribute("unidadAdministrativa", "java.lang.String[]","Departamento,Area,Direccion", true, "Unidad Administrativa", "Indique que tipo de unidad administrativa"));                        
            attributes.add(buildAttribute("denominacionPuesto", String.class.getName(), null, false, "Denominacion del Puesto", "Indique la denominación del puesto"));
            attributes.add(buildAttribute("fechaInicio", Date.class.getName(), ago.getTime(), false, "Fecha de Ingreso", "Fecha de ingreso del trabajar"));
            attributes.add(buildAttribute("fechaFin", Date.class.getName(), ago.getTime(), false, "Fecha de Salida", "Fecha de salida del trabajo"));            
            attributes.add(buildAttribute("responsabilidades", String.class.getName(), null, false, "Principales Responsabilidades", "Indique que responsabilidades tenia"));
            attributes.add(buildAttribute("motivoSalida", String.class.getName(), null, false, "Motivo de Salida", "Por que motivo salio"));
            
            //Agregar atributos
            structure.setProperties(attributes);

            bussinesEntityType.addStructure(structure);

            entityManager.persist(bussinesEntityType);
            entityManager.flush();
        }
    }
    
    //builder for attributes
    private Property buildAttribute(String name, String type, Serializable value, boolean required, String label, String helpinline) {
        Property attributte = new Property();
        attributte.setName(name);
        attributte.setType(type);
        attributte.setValue(value);
        attributte.setRequired(required);
        attributte.setLabel(label);
        attributte.setHelpInline(helpinline);
        return attributte;
    }
    
    //builder for attributes
    private Property buildAttribute(String groupName, String name, String type, Serializable value, boolean required, String label, String helpinline) {
        Property attributte = new Property();
        attributte.setGroupName(groupName);
        attributte.setName(name);
        attributte.setType(type);
        attributte.setValue(value);
        attributte.setRequired(required);
        attributte.setLabel(label);
        attributte.setHelpInline(helpinline);
        return attributte;
    }

    private Property buildAttribute(String name, String type, Serializable value, boolean required, String label, String helpinline, String render) {
        Property attributte = new Property();
        
        attributte.setName(name);
        attributte.setType(type);
        attributte.setValue(value);
        attributte.setRequired(required);
        attributte.setLabel(label);
        attributte.setHelpInline(helpinline);
        attributte.setRender(render);
        return attributte;
    }

    private Property buildAttribute(String name, String type, Serializable value, boolean required, String label, String helpinline, String render, String customForm) {
        Property attributte = new Property();
        attributte.setName(name);
        attributte.setType(type);
        attributte.setValue(value);
        attributte.setRequired(required);
        attributte.setLabel(label);
        attributte.setHelpInline(helpinline);
        attributte.setRender(render);
        attributte.setCustomForm(customForm);
        return attributte;
    }

    //metodo buildProterty
    private Property builtProperty(String name, String type, Serializable value, boolean required, String label, String helpinline, String render, String customForm){
        Property property = new Property();
        property.setName(name);
        property.setType(type);
        property.setValue(value);
        property.setRequired(required);
        property.setLabel(label);
        property.setHelpInline(helpinline);
        property.setRender(render);
        property.setCustomForm(customForm);        
        return property;
    }
}
