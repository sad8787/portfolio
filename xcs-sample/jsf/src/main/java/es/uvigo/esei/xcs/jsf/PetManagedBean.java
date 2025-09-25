package es.uvigo.esei.xcs.jsf;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import es.uvigo.esei.xcs.domain.entities.AnimalType;
import es.uvigo.esei.xcs.domain.entities.Pet;
import es.uvigo.esei.xcs.service.PetService;

@Named("pet")
@RequestScoped
public class PetManagedBean {
	@Inject
	private PetService service;
	
	private String name;
	private Date birth;
	private AnimalType animal;
	
	private Integer id;
	
	private String errorMessage;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getBirth() {
		return birth;
	}

	public void setBirth(Date birth) {
		this.birth = birth;
	}

	public String getAnimal() {
		return Optional.ofNullable(this.animal)
			.map(AnimalType::name)
		.orElse(null);
	}

	public void setAnimal(String animal) {
		this.animal = AnimalType.valueOf(animal);
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public boolean isError() {
		return this.errorMessage != null;
	}
	
	public boolean isEditing() {
		return this.id != null;
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public List<Pet> getPets() {
		return this.service.list();
	}
	
	public String edit(int petId) {
		final Pet pet = this.service.get(petId);
		
		this.id = pet.getId();
		this.name = pet.getName();
		this.birth = pet.getBirth();
		this.animal = pet.getAnimal();
		
		return this.getViewId();
	}
	
	public String cancelEditing() {
		this.clear();
		
		return this.getViewId();
	}

	public String remove(int id) {
		this.service.remove(id);

		return redirectTo(this.getViewId());
	}
	
	public String store() {
		try {
			if (this.isEditing()) {
				final Pet pet = this.service.get(this.id);
				pet.setName(this.name);
				pet.setBirth(this.birth);
				pet.setAnimal(this.animal);
				
				this.service.update(pet);
			} else {
				this.service.create(new Pet(name, animal, birth));
			}
			
			this.clear();
			
			return redirectTo(this.getViewId());
		} catch (Throwable t) {
			this.errorMessage = t.getMessage();
			
			return this.getViewId();
		}
	}
	
	private void clear() {
		this.name = null;
		this.birth = null;
		this.animal = null;
		this.id = null;
		this.errorMessage = null;
	}

	private String redirectTo(String url) {
		return url  + "?faces-redirect=true";
	}

	private String getViewId() {
		return FacesContext.getCurrentInstance().getViewRoot().getViewId();
	}
}
