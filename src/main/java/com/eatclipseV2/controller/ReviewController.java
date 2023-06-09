package com.eatclipseV2.controller;

import com.eatclipseV2.common.MessageDto;
import com.eatclipseV2.common.StringConst;
import com.eatclipseV2.domain.review.dto.ReviewFormDto;
import com.eatclipseV2.entity.Member;
import com.eatclipseV2.entity.Review;
import com.eatclipseV2.entity.Shop;
import com.eatclipseV2.service.ReviewService;
import com.eatclipseV2.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final ShopService shopService;

    @GetMapping("/member")
    public String reviews(@SessionAttribute(name = StringConst.LOGIN_MEMBER) Member loginMember,
                          Model model) {

        model.addAttribute("member", loginMember);
        List<Review> allReviews = reviewService.findAllReviews();
        model.addAttribute("reviews", allReviews);

        return "reviews/reviewListByMember";
    }

    @GetMapping("/shop")
    public String reviews(@SessionAttribute(name = StringConst.LOGIN_SHOP) Shop loginShop,
                          Model model) {

        model.addAttribute("shop", loginShop);
        List<Review> reviews = reviewService.findReviewsByShop(loginShop.getId());
        model.addAttribute("reviews", reviews);

        return "reviews/reviewListByShop";
    }

    @GetMapping("/member/{reviewId}")
    public String reviewDtlByMember(@SessionAttribute(name = StringConst.LOGIN_MEMBER) Member loginMember,
                            @PathVariable Long reviewId,
                            Model model) {

        model.addAttribute("member", loginMember);
        Review review = reviewService.findReview(reviewId);
        model.addAttribute("review", review);

        return "reviews/reviewDtlByMember";
    }

    @PostMapping("/member/{reviewId}/delete")
    public String reviewDelete(@SessionAttribute(name = StringConst.LOGIN_MEMBER) Member loginMember,
                               @PathVariable Long reviewId,
                               Model model) {

        model.addAttribute("member", loginMember);

        reviewService.deleteReview(reviewId);

        MessageDto messageDto = new MessageDto("리뷰가 삭제되었습니다",
                "/reviews/member", RequestMethod.GET, null);
        return showMessageAndRedirect(messageDto, model);
    }

    @GetMapping("/shop/{reviewId}")
    public String reviewDtlByShop(@SessionAttribute(name = StringConst.LOGIN_SHOP) Shop loginShop,
                            @PathVariable Long reviewId,
                            Model model) {

        model.addAttribute("shop", loginShop);
        Review review = reviewService.findReview(reviewId);
        model.addAttribute("review", review);

        return "reviews/reviewDtlByShop";
    }

    @GetMapping("/new")  // TODO: 2023-05-11 011 update Review 로직 추가 예정
    public String writeReview(@SessionAttribute(name = StringConst.LOGIN_MEMBER) Member loginMember,
                              @ModelAttribute ReviewFormDto reviewFormDto, Model model) {

        model.addAttribute("member", loginMember);
        List<Shop> allShop = shopService.findAllShop();
        model.addAttribute("allShop", allShop);

        return "reviews/reviewWriteForm";
    }

    @PostMapping("/new")
    public String writeReview(@SessionAttribute(name = StringConst.LOGIN_MEMBER) Member loginMember,
                      @Valid ReviewFormDto reviewFormDto, BindingResult bindingResult,
                      Model model) {

        model.addAttribute("member", loginMember);

        if (bindingResult.hasErrors()) {
            List<Shop> allShop = shopService.findAllShop();
            model.addAttribute("allShop", allShop);
            return "reviews/reviewWriteForm";
        }
        Shop shop = shopService.findShop(reviewFormDto.getShopId());
        Review review = reviewService.saveReview(reviewFormDto, loginMember.getId(), shop.getId());

        MessageDto messageDto = new MessageDto("리뷰가 작성되었습니다",
                "/reviews/member/" + review.getId(), RequestMethod.GET, null);
        return showMessageAndRedirect(messageDto, model);
    }

    @GetMapping("/update")
    public String updateReview(@SessionAttribute(name = StringConst.LOGIN_MEMBER) Member loginMember,
                               Model model,  // @ModelAttribute ReviewFormDto reviewFormDto,
                               @RequestParam(required = false) Long reviewId) {

        model.addAttribute("member", loginMember);

        Review review = reviewService.findReviewById(reviewId);
        Shop shop = review.getShop();
        ReviewFormDto reviewFormDto = ReviewFormDto.of(review);
        model.addAttribute("reviewFormDto", reviewFormDto);
        model.addAttribute("shop", shop);
        return "reviews/reviewUpdateForm";
    }

    @PostMapping("/update")
    public String updateReview(@SessionAttribute(name = StringConst.LOGIN_MEMBER) Member loginMember,
                               @Valid ReviewFormDto reviewFormDto, BindingResult bindingResult,
                               Model model) {

        model.addAttribute("member", loginMember);

        if (bindingResult.hasErrors()) {
            List<Shop> allShop = shopService.findAllShop();
            model.addAttribute("allShop", allShop);
            return "reviews/reviewUpdateForm";
        }

        reviewService.update(reviewFormDto);

        MessageDto message = new MessageDto("리뷰가 수정되었습니다.",
                "/reviews/member/"+reviewFormDto.getId(), RequestMethod.GET, null);
        return showMessageAndRedirect(message, model);
    }

    private String showMessageAndRedirect(final MessageDto messageDto, Model model) {
        model.addAttribute("params", messageDto);
        return "common/messageRedirect";
    }
}
