package com.whj.Service.Impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.whj.Config.AlipayConfig;
import com.whj.Mapper.AdminMapper;
import com.whj.Pojo.*;
import com.whj.Service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private AlipayConfig alipayConfig;

    @Override
    public List<User> selectUser() {
        List<User> users = (List<User>) redisTemplate.opsForValue().get("AdminUser");
        if (users == null) {
            users = adminMapper.selectUser();
            redisTemplate.opsForValue().set("AdminUser", users);
        }
        return users;
    }

    @Override
    public String updateUserLock(User user) {
        redisTemplate.delete("AdminUser");
        int result = adminMapper.updateUserLock(user);
        if (result > 0)
            return "success";
        else
            return "false";
    }

    @Transactional
    @Override
    public String deleteUser(int id) {
        redisTemplate.delete("AdminUser");
        int result = adminMapper.deleteUserRole(id);
        if (result <= 0)
            return "false";
        result = adminMapper.deleteUser(id);
        return "success";
    }

    @Override
    public User User(int id) {
        return adminMapper.User(id);
    }

    @Override
    public String updateUser(User user) {
        redisTemplate.delete("AdminUser");
        int result = adminMapper.updateUser(user);
        if (result > 0)
            return "success";
        else
            return "false";
    }

    @Transactional
    @Override
    public String deleteAll(String checkbox) {
        checkbox = checkbox.substring(1, checkbox.length() - 1);
        String[] idss = checkbox.split(",");
        int[] id = new int[idss.length - 1];
        for (int i = 1; i < idss.length; i++) {
            id[i - 1] = Integer.parseInt(idss[i]);
        }
        int result = adminMapper.deleteAll(id);
        if (result > 0) {
            redisTemplate.delete("AdminUser");
            return "success";
        } else
            return "false";
    }

    @Override
    public List<Pet> selectPet() {
        List<Pet> pet = (List<Pet>) redisTemplate.opsForValue().get("Pet");
        if (pet == null) {
            pet = adminMapper.selectPet();
            redisTemplate.opsForValue().set("Pet", pet);
        }
        return pet;
    }

    @Override
    public List<Pet_type> selectType() {
        //redis缓存查询数据
        List<Pet_type> pet_types = (List<Pet_type>) redisTemplate.opsForValue().get("pet_type");
        if (pet_types == null) {
            pet_types = adminMapper.selectType();
            redisTemplate.opsForValue().set("pet_type", pet_types);
        }
        return pet_types;
    }

    @Override
    public String addPet(MultipartFile file, Pet pet) {
        String filename;
        try {
            //上传图片
            filename = System.currentTimeMillis() + file.getOriginalFilename();
            String str = System.getProperty("user.dir");
            str = str.replaceAll("\\\\", "/");
            String destfilename = str + "/src/main/resources/static/images/"
                    + filename;
            pet.setPet_image("/images/" + filename);
            //存入数据库格式化时间
            Date date = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pet.setUpshelf_time(dateFormat.format(date));
            int result = adminMapper.AddPet(pet);
            if (result > 0) {
                File destfile = new File(destfilename);
                file.transferTo(destfile);
                redisTemplate.delete("Pet");
                return "success";
            } else
                return "false";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "uploadfalse";
        } catch (IOException e) {
            e.printStackTrace();
            return "uploadfalse";
        }
    }

    @Override
    public String deletePet(int id) {
        int result = adminMapper.deletePet(id);
        if (result > 0) {
            //全部宠物缓存
            redisTemplate.delete("Pet");
            //单个宠物缓存
            redisTemplate.delete("Pet" + id);
            redisTemplate.delete("HotPet");
            return "success";
        } else
            return "false";
    }

    @Override
    public Pet Pet(int id) {
        Pet pet = (Pet) redisTemplate.opsForValue().get("Pet" + id);
        if (pet == null) {
            pet = adminMapper.Pet(id);
            redisTemplate.opsForValue().set("Pet" + id, pet);
        }
        return pet;
    }

    @Override
    public String updatePet(MultipartFile file, Pet pet) {
        String filename;
        if(!file.isEmpty()) {
            try {
                //上传图片
                filename = System.currentTimeMillis() + file.getOriginalFilename();
                String str = System.getProperty("user.dir");
                str = str.replaceAll("\\\\", "/");
                String destfilename = str + "/src/main/resources/static/images/"
                        + filename;
                pet.setPet_image("/images/" + filename);
                Pet pet1 = adminMapper.Pet(pet.getPet_id());
                int result = adminMapper.updatePet(pet);
                if (result > 0) {
                    File destfile = new File(destfilename);
                    file.transferTo(destfile);
                    File file1 = new File(str + "/src/main/resources/static/" + pet1.getPet_image());
                    file1.delete();
                    redisTemplate.delete("Pet");
                    redisTemplate.delete("Pet" + pet.getPet_id());
                    redisTemplate.delete("HotPet");
                    return "success";
                } else
                    return "false";
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return "uploadfalse";
            } catch (IOException e) {
                e.printStackTrace();
                return "uploadfalse";
            }
        }
        else{
            pet.setPet_image(null);
            int result = adminMapper.updatePet(pet);
            if(result > 0){
                redisTemplate.delete("Pet");
                redisTemplate.delete("Pet" + pet.getPet_id());
                redisTemplate.delete("HotPet");
                return "success";
            }
            else
                return "false";
        }
    }

    @Override
    public String deleteAllPet(String checkbox) {
        checkbox = checkbox.substring(1,checkbox.length()-1);
        String[] idss = checkbox.split(",");
        int[] id = new int[idss.length-1];
        for (int i=1; i<idss.length; i++){
            id[i-1] = Integer.parseInt(idss[i]);
        }
        List<String> list = new ArrayList<>();
        Pet pet;
        for(int i:id){
            pet = adminMapper.Pet(i);
            list.add(pet.getPet_image());
        }
        int result = adminMapper.deleteAllPet(id);
        if (result > 0) {
            //全部宠物缓存
            redisTemplate.delete("Pet");
            redisTemplate.delete("HotPet");
            //单个宠物缓存
            for (int i : id)
                redisTemplate.delete("Pet" + i);
            //删除宠物图片
            String str = System.getProperty("user.dir");
            str = str.replaceAll("\\\\", "/");
            for(String images : list){
                File file1 = new File(str + "/src/main/resources/static/"+images);
                file1.delete();
            }
            return "success";
        } else
            return "false";
    }

    @Transactional
    @Override
    public Integer deletePetType(int id) {
        int result = adminMapper.PetType(id);
        if (result > 0)
            return -2;
        result = adminMapper.deletePetType(id);
        if (result > 0) {
            redisTemplate.delete("pet_type");
            redisTemplate.delete("pet_type" + id);
        }
        return result;
    }

    @Override
    public Pet_type pettype(int id) {
        Pet_type pet_type = (Pet_type) redisTemplate.opsForValue().get("pet_type" + id);
        if (pet_type == null) {
            pet_type = adminMapper.pettype(id);
            redisTemplate.opsForValue().set("pet_type" + id, pet_type);
        }
        return pet_type;
    }

    @Override
    public String updatePetType(Pet_type pet_type) {
        int result = adminMapper.updatePetType(pet_type);
        if (result > 0) {
            redisTemplate.delete("pet_type");
            redisTemplate.delete("pet_type" + pet_type.getId());
            return "success";
        } else
            return "false";
    }

    @Override
    public String addPetType(String type) {
        int result = adminMapper.addPetType(type);
        if (result > 0) {
            redisTemplate.delete("pet_type");
            return "success";
        } else
            return "false";
    }

    @Override
    public String deleteAllPetType(String checkbox) {
        checkbox = checkbox.substring(1, checkbox.length() - 1);
        String[] idss = checkbox.split(",");
        int[] id = new int[idss.length];
        for (int i = 1; i < idss.length; i++) {
            id[i] = Integer.parseInt(idss[i]);
        }
        List<Integer> list = adminMapper.AllPetType(id);
        if (list.size() > 0)
            return "typefalse";
        int result = adminMapper.deleteAllPetType(id);
        if (result > 0) {
            redisTemplate.delete("pet_type");
            for (int i : id)
                redisTemplate.delete("pet_type" + i);
            return "success";
        } else
            return "false";
    }

    @Override
    public List<Ord> orderlist() {
        List<Ord> ordList = (List<Ord>) redisTemplate.opsForValue().get("ordList");
        if (ordList == null) {
            ordList = adminMapper.orderlist();
            redisTemplate.opsForValue().set("ordList", ordList);
        }
        return ordList;
    }

    @Override
    public String order(String id,Double price_sum, int status_ord) throws AlipayApiException {
        if(status_ord == -1){
            int result = adminMapper.order(id, status_ord);
            if (result > 0) {
                redisTemplate.delete("ordList");
                redisTemplate.delete("ord"+id);
                redisTemplate.delete(redisTemplate.keys("ordList"+"*"));
                return "success";
            } else
                return "false";
        }
        else{
            // 初始化的AlipayClient
            AlipayClient apAlipayClient
                    = new DefaultAlipayClient(alipayConfig.gatewayUrl,
                    alipayConfig.app_id, alipayConfig.merchant_private_key, "json",
                    alipayConfig.charset, alipayConfig.alipay_public_key, alipayConfig.sign_type);
            AlipayTradeRefundRequest alipayRequest = new AlipayTradeRefundRequest();
            alipayRequest.setBizContent("{\"out_trade_no\":\"" + id + "\","
                    + "\"refund_amount\":\"" + price_sum + "\"}");
//                + "\"refund_reason\":\"" + map.get("refund_reason") + "\","
//                + "\"out_request_no\":\"" + map.get("out_request_no") + "\"}");
            AlipayTradeRefundResponse aliPayResponse = apAlipayClient.execute(alipayRequest);
            if("Y".equals(aliPayResponse.getFundChange())){
                adminMapper.order(id, status_ord);
                redisTemplate.delete("ordList");
                redisTemplate.delete(redisTemplate.keys("ordList"+"*"));
                redisTemplate.delete("ord"+id);
                return "success";
            }
            return "false";
        }
    }

    @Override
    public List<Rotation> selectRotation() {
        //redis缓存查询数据
        List<Rotation> rotationList = (List<Rotation>) redisTemplate.opsForValue().get("rotation");
        if (rotationList == null) {
            rotationList = adminMapper.selectRotation();
            redisTemplate.opsForValue().set("rotation", rotationList);
        }
        return rotationList;
    }

    @Override
    public String addRotation(MultipartFile file) {
        String filename;
        try {
            //上传图片
            filename = System.currentTimeMillis() + file.getOriginalFilename();
            String str = System.getProperty("user.dir");
            str = str.replaceAll("\\\\", "/");
            String destfilename = str + "/src/main/resources/static/images/"
                    + filename;
            File destfile = new File(destfilename);
            file.transferTo(destfile);
            Rotation rotation = new Rotation();
            rotation.setImages("/images/" + filename);
            int result = adminMapper.addRotation(rotation);
            if (result > 0) {
                redisTemplate.delete("rotation");
                return "success";
            } else
                return "false";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "uploadfalse";
        } catch (IOException e) {
            e.printStackTrace();
            return "uploadfalse";
        }
    }

    @Override
    public String updateRotation(MultipartFile file, int id) {
        Rotation rotation = new Rotation();
        rotation.setId(id);
        String filename;
        try {
            //上传图片
            filename = System.currentTimeMillis() + file.getOriginalFilename();
            String str = System.getProperty("user.dir");
            str = str.replaceAll("\\\\", "/");
            String destfilename = str + "/src/main/resources/static/images/"
                    + filename;
            rotation.setImages("/images/" + filename);
            Rotation rotation1 = adminMapper.selectOneRotataion(rotation.getId());
            int result = adminMapper.updateRotation(rotation);
            if (result > 0) {
                File destfile = new File(destfilename);
                file.transferTo(destfile);
                redisTemplate.delete("rotation");
                File file1 = new File(str + "/src/main/resources/static" + rotation1.getImages());
                file1.delete();
                return "success";
            } else
                return "false";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "uploadfalse";
        } catch (IOException e) {
            e.printStackTrace();
            return "uploadfalse";
        }

    }

    @Override
    public String deleteRotation(int id) {
        Rotation rotation = adminMapper.selectOneRotataion(id);
        String str = System.getProperty("user.dir");
        str = str.replaceAll("\\\\", "/");
        File file = new File(str + "/src/main/resources/static" + rotation.getImages());
        int result = adminMapper.deleteRotation(id);
        if (result > 0) {
            file.delete();
            redisTemplate.delete("rotation");
            return "success";
        } else
            return "false";
    }

    @Override
    public String deleteAllRotation(String checkbox) {
        checkbox = checkbox.substring(1, checkbox.length() - 1);
        String[] idss = checkbox.split(",");
        int[] id = new int[idss.length - 1];
        for (int i = 1; i < idss.length; i++) {
            id[i - 1] = Integer.parseInt(idss[i]);
        }
        List<String> list = new ArrayList<>();
        Rotation rotation;
        for (int i : id) {
            rotation = adminMapper.selectOneRotataion(i);
            list.add(rotation.getImages());
        }
        String str = System.getProperty("user.dir");
        str = str.replaceAll("\\\\", "/");
        int result = adminMapper.deleteAllRotation(id);
        if (result > 0) {
            redisTemplate.delete("rotation");
            File file;
            for (String images : list) {
                file = new File(str + "/src/main/resources/static" + images);
                file.delete();
            }
            return "success";
        } else
            return "false";
    }

}
