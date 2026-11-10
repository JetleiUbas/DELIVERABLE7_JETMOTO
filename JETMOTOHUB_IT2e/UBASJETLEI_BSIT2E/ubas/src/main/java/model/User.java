package model;

/**
 * Data model for a JetMotoHub user.
 * Roles: ADMIN, STAFF, or CUSTOMER.
 * Status: PENDING by default after registration.
 */
public record User(int id, String name, String username, String password, String roles, String status) {
}
