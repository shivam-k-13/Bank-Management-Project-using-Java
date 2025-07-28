import java.util.List;

public class AdminService {
    private static final String ADMIN_FILE = "admins.dat";
    private final List<Admin> admins;

    public AdminService() {
        this.admins = FileUtil.readFromFile(ADMIN_FILE);
        if (this.admins.isEmpty()) {
            this.admins.add(new Admin("admin", "admin"));
            FileUtil.writeToFile(ADMIN_FILE, this.admins);
        }
    }

    public Admin getAdminByUsername(String username) {
        for (Admin admin : admins) {
            if (admin.getUsername().equals(username)) {
                return admin;
            }
        }
        return null;
    }
}