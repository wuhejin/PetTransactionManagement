package com.whj.Service.Impl;

import com.whj.Mapper.IndexMapper;
import com.whj.Pojo.Pet;
import com.whj.Pojo.Pet_type;
import com.whj.Pojo.Rotation;
import com.whj.Service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class IndexServiceImpl implements IndexService {

    @Autowired
    private IndexMapper indexMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Pet_type> selectType() {
        //redis缓存查询数据
        List<Pet_type> pet_types =(List<Pet_type>) redisTemplate.opsForValue().get("pet_type");
        if(pet_types == null){
            pet_types = indexMapper.selectType();
            redisTemplate.opsForValue().set("pet_type",pet_types);
        }
        return pet_types;
    }

    @Override
    public List<Pet> selectPet() {
        List<Pet> pets =(List<Pet>) redisTemplate.opsForValue().get("HotPet");
        if(pets == null){
            pets = indexMapper.selectPet();
            redisTemplate.opsForValue().set("HotPet",pets);
        }
        return pets;
    }

    @Override
    public List<Pet> selectPetAll(Pet pet) {
        return indexMapper.selectPetAll(pet);
    }

    @Override
    public Pet Pet(int id) {
        Pet pet = (Pet) redisTemplate.opsForValue().get("Pet"+id);
        if(pet == null){
            pet = indexMapper.Pet(id);
            redisTemplate.opsForValue().set("Pet"+id,pet);
        }
        return pet;
    }

    @Override
    public List<Rotation> selectRotation() {
        //redis缓存查询数据
        List<Rotation> rotationList = (List<Rotation>) redisTemplate.opsForValue().get("rotation");
        if (rotationList == null) {
            rotationList = indexMapper.selectRotation();
            redisTemplate.opsForValue().set("rotation", rotationList);
        }
        return rotationList;
    }
}
