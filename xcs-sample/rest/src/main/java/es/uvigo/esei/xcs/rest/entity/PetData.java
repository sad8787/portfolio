package es.uvigo.esei.xcs.rest.entity;

import java.io.Serializable;
import java.util.Date;

import es.uvigo.esei.xcs.domain.entities.AnimalType;
import es.uvigo.esei.xcs.domain.entities.Pet;

public class PetData implements Serializable {
  private static final long serialVersionUID = 1L;
  
  private String name;
  private AnimalType animal;
  private Date birth;

  PetData() {}

  public PetData(String name, AnimalType animal, Date birth) {
    this.name = name;
    this.animal = animal;
    this.birth = birth;
  }

  public String getName() {
    return name;
  }

  public AnimalType getAnimal() {
    return animal;
  }

  public Date getBirth() {
    return birth;
  }
  
  public Pet assignData(Pet pet) {
    pet.setName(this.name);
    pet.setAnimal(this.animal);
    pet.setBirth(this.birth);
    
    return pet;
  }
  
  public Pet toPet() {
    return new Pet(this.name, this.animal, this.birth);
  }

}
