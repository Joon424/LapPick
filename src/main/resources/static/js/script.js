// basic-N42 [qfM47rgHSq]
(function() {
  $(function() {
    $(".basic-N42").each(function() {
      const $block = $(this);
      // Swiper
      const swiper = new Swiper(".basic-N42 .contents-swiper", {
        slidesPerView: 'auto',
        spaceBetween: 0,
        loop: true,
        autoplay: {
          delay: 5000,
        },
        navigation: {
          nextEl: ".basic-N42 .swiper-button-next",
          prevEl: ".basic-N42 .swiper-button-prev",
        },
        pagination: {
          type: "progressbar",
          el: ".basic-N42 .swiper-pagination",
          clickable: true,
        },
      });
    });
  });
})();
// basic-N50 [zqM3qV2rT5]
(function() {
  $(function() {
    $(".basic-N50").each(function() {
      const $block = $(this);
      const $dim = $block.find('.contents-dim');
      // Mobile Filter Open
      $block.find('.btn-filter').on('click', function() {
        $block.addClass('filter-active');
        $dim.fadeIn();
      });
      // Mobile Filter Close
      $block.find('.btn-close, .contents-dim').on('click', function() {
        $block.removeClass('filter-active');
        $dim.fadeOut();
      });
    });
  });
})();
// basic-N51 [Wkm3qv4x0r]
(function() {
  $(function() {
    $(".basic-N51").each(function() {
      const $block = $(this);
      const $thumbnail = $block.find('.contents-thumbnail .contents-thumbimg');
      const $thumbitem = $block.find('.contents-thumbitem .contents-thumbimg');
      // Thumbnail Click Event
      $thumbitem.on("click", changePic);

      function changePic() {
        const newPic = $(this).attr("src");
        $thumbnail.attr("src", newPic);
      }
      // Like Button Click Event
      $block.find('.btn-like-line').on('click', function() {
        $block.find('.contents-brand-group').addClass('like-on');
      });
      $block.find('.btn-like-fill').on('click', function() {
        $block.find('.contents-brand-group').removeClass('like-on');
      });
      // Amount Count Button Click Event
      $block.find(".contents-amount").each(function() {
        const $this = $(this);
        const $amountNumElement = $this.find(".contents-amount-num");
        $this.on("click", ".btn-minus", function() {
          let amountNum = parseInt($amountNumElement.text());
          if (amountNum > 1) {
            amountNum--;
          }
          $amountNumElement.text(amountNum);
        });
        $this.on("click", ".btn-plus", function() {
          let amountNum = parseInt($amountNumElement.text());
          amountNum++;
          $amountNumElement.text(amountNum);
        });
      });
    });
  });
})();
// basic-N39 [Zdm4HzsGXN]
(function() {
  $(function() {
    $(".basic-N39").each(function() {
      const $block = $(this);
      // Swiper
      const swiper = new Swiper(".basic-N39 .contents-swiper", {
        slidesPerView: 1,
        spaceBetween: 0,
        loop: true,
        autoplay: {
          delay: 5000,
        },
        loop: true,
        pagination: {
          el: ".basic-N39 .swiper-pagination",
          type: "fraction",
          clickable: true,
        },
        navigation: {
          nextEl: ".basic-N39 .swiper-button-next",
          prevEl: ".basic-N39 .swiper-button-prev",
        },
      });
      // Swiper Play, Pause Button
      const pauseButton = $block.find('.swiper-button-pause');
      const playButton = $block.find('.swiper-button-play');
      playButton.hide();
      pauseButton.show();
      pauseButton.on('click', function() {
        swiper.autoplay.stop();
        playButton.show();
        pauseButton.hide();
      });
      playButton.on('click', function() {
        swiper.autoplay.start();
        playButton.hide();
        pauseButton.show();
      });
    });
  });
})();