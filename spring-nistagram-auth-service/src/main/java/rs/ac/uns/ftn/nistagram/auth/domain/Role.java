package rs.ac.uns.ftn.nistagram.auth.domain;

import java.util.ArrayList;
import java.util.List;

public class Role {
    private String id;
    private final List<Permission> allowedActions = new ArrayList<>();
}