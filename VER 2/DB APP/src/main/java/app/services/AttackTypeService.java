package app.services;

import app.dao.AttackTypeDAO;
import app.models.AttackType;
import java.util.List;

/**
 * Service for AttackType business logic
 */
public class AttackTypeService {
    private final AttackTypeDAO attackTypeDAO = new AttackTypeDAO();
    
    public boolean createAttackType(String name, String description, String severityLevel) {
        AttackType attackType = new AttackType(name, description, severityLevel);
        int id = attackTypeDAO.insert(attackType);
        return id > 0;
    }
    
    public List<AttackType> getAllAttackTypes() {
        return attackTypeDAO.findAll();
    }
    
    public AttackType getAttackTypeById(int attackTypeID) {
        return attackTypeDAO.findById(attackTypeID);
    }
    
    public boolean updateAttackType(int attackTypeID, String name, String description, String severityLevel) {
        AttackType attackType = new AttackType(name, description, severityLevel);
        attackType.setAttackTypeID(attackTypeID);
        return attackTypeDAO.update(attackType);
    }
    
    public boolean deleteAttackType(int attackTypeID) {
        return attackTypeDAO.delete(attackTypeID);
    }
}

