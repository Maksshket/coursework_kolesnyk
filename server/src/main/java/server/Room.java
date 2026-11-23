package server;

public class Room {
    private Long id;
    private String roomNumber;
    private int floor;
    private String roomType;
    private int capacity;
    private String equipment;

    public Room() {}

    public Room(Long id, String roomNumber, int floor, String roomType, int capacity, String equipment) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.roomType = roomType;
        this.capacity = capacity;
        this.equipment = equipment;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }
}
