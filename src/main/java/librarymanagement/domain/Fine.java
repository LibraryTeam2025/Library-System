package librarymanagement.domain;

public class Fine {
    private User user;
    private int amount;
    private boolean paid;

    public Fine(User user, int amount) {
        this.user = user;
        this.amount = amount;
        this.paid = false;
        user.getFines().add(this);
    }

    public int getAmount() { return amount; }
    public boolean isPaid() { return paid; }
    public void pay(int payment) {
        if(payment >= amount) { paid = true; amount = 0; }
        else amount -= payment;
    }
}
