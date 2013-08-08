package hello;

import javax.inject.Inject;

import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.FacebookProfile;
import org.springframework.social.facebook.api.PagedList;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/")
public class HelloController {

    private Facebook facebook;

    @Inject
    public HelloController(Facebook facebook) {
        this.facebook = facebook;
    }

    @RequestMapping(method=RequestMethod.GET)
    public String helloFacebook(Model model) {
        if (!facebook.isAuthorized()) {
            return "redirect:/connect/facebook";
        }

        model.addAttribute(facebook.userOperations().getUserProfile());
        PagedList<FacebookProfile> friends = facebook.friendOperations().getFriendProfiles();
        model.addAttribute("friends", friends);

        return "hello";
    }

}
