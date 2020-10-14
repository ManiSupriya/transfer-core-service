<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="viewport" content="width=device-width" />

    <title>Mashreq Email</title>

    <!-- CSS Reset : BEGIN -->
    <style type="text/css">
      html,
      body {
        margin: 0 auto !important;
        padding: 0 !important;
        height: 100% !important;
        width: 100% !important;
        background: #ffffff;
      }

      /* What it does: Stops email clients resizing small text. */
      * {
        -ms-text-size-adjust: 100%;
        -webkit-text-size-adjust: 100%;
      }
      /* What it does: Centers email on Android 4.4 */
      div[style*="margin: 16px 0"] {
        margin: 0 !important;
      }
      /* What it does: Uses a better rendering method when resizing images in IE. */
      img {
        -ms-interpolation-mode: bicubic;
      }

      /* What it does: A work-around for email clients meddling in triggered links. */
      *[x-apple-data-detectors],  /* iOS */
      .unstyle-auto-detected-links *,
      .aBn {
        border-bottom: 0 !important;
        cursor: default !important;
        color: inherit !important;
        text-decoration: none !important;
        font-size: inherit !important;
        font-family: inherit !important;
        font-weight: inherit !important;
        line-height: inherit !important;
      }

      /* What it does: Prevents Gmail from changing the text color in conversation threads. */
      .img {
        color: inherit !important;
      }

      /* If the above doesn't work, add a .g-img class to any image in question. */
      img.g-img + div {
        display: none !important;
      }

      /* What it does: Removes right gutter in Gmail iOS app: https://github.com/TedGoas/Cerberus/issues/89  */
      /* Create one of these media queries for each additional viewport size you'd like to fix */

      /* iPhone 4, 4S, 5, 5S, 5C, and 5SE */
      @media only screen and (min-device-width: 320px) and (max-device-width: 374px) {
        u ~ div .email-container {
          min-width: 320px !important;
        }
      }
      /* iPhone 6, 6S, 7, 8, and X */
      @media only screen and (min-device-width: 375px) and (max-device-width: 413px) {
        u ~ div .email-container {
          min-width: 375px !important;
        }
      }
      /* iPhone 6+, 7+, and 8+ */
      @media only screen and (min-device-width: 414px) {
        u ~ div .email-container {
          min-width: 414px !important;
        }
      }
    </style>

    <!-- CSS Reset : END -->

    <style type="text/css">
      /* GLOBAL */
      * {
        margin: 0;
        padding: 0;
      }
      * {
        font-family: "Helvetica Neue", "Helvetica", Helvetica, Arial, sans-serif;
      }

      .collapse {
        margin: 0;
        padding: 0;
      }

      .bg_white {
        background: #ffffff;
      }
      .bg_light {
        background: #fafafa;
      }
      .bg_black {
        background: #000000;
      }
      .bg_dark {
        background: rgba(0, 0, 0, 0.8);
      }
      .text_black {
        color: #000000;
        font-family: "Helvetica Neue", "Helvetica", Helvetica, Arial, sans-serif;
      }
      .text_light {
        color: #6e6e6e;
        font-family: "Helvetica Neue", "Helvetica", Helvetica, Arial, sans-serif;
      }

      /* ELEMENTS */

      .link {
        color: #313131;
      }

      /* BUTTON */
      .btn {
        text-decoration: none;
        color: #fff;
        background-color: #666;
        padding: 10px 16px;
        font-weight: inherit;
        margin-right: 10px;
        text-align: center;
        cursor: pointer;
        display: inline-block;
        box-shadow: none;
        border-radius: 3px;
      }

      /* HEADER */
      table.head-wrap {
        width: 600px;
        margin: 0 auto;
        padding: 20px 0;
      }

      .header.email-container table td.logo {
        padding: 15px;
      }
      .header .content table tr td {
        text-align: center;
      }

      /* BODY */
      table.body-wrap {
        width: 100%;
      }

      /* FOOTER */

      table.social {
        /* 	padding:15px; */
        background-color: #eff1f5;
      }

      table.footer-wrap {
        width: 100%;
        clear: both !important;
      }
      .footer-wrap .email-container td.content p {
        border-top: 1px solid rgb(215, 215, 215);
        padding-top: 15px;
      }
      .footer-wrap .email-container td.content p {
        font-size: 10px;
        font-weight: bold;
      }

      /* TYPOGRAPHY */
      h1,
      h2,
      h3,
      h4,
      h5,
      h6 {
        font-family: "HelveticaNeue-Light", "Helvetica Neue Light",
          "Helvetica Neue", Helvetica, Arial, "Lucida Grande", sans-serif;
        line-height: 1.1;
        margin-bottom: 15px;
        color: #313131;
      }
      h1 small,
      h2 small,
      h3 small,
      h4 small,
      h5 small,
      h6 small {
        font-size: 60%;
        color: #6f6f6f;
        line-height: 0;
        text-transform: none;
      }

      h1 {
        font-weight: bold;
        font-size: 26px;
      }
      h2 {
        font-weight: bold;
        font-size: 20px;
      }
      h3 {
        font-weight: bold;
        font-size: 16px;
      }

      .collapse {
        margin: 0 !important;
      }

      p,
      ul {
        margin-bottom: 10px;
        font-weight: normal;
        font-size: 16px;
        line-height: 1.6;
        color:#313131;
      }
      
      p.last {
        margin-bottom: 0px;
      }

      .email-container {
        display: block !important;
        max-width: 600px !important;
        margin: 0 auto !important; /* makes it centered */
        clear: both !important;
      }

      .content {
        padding: 0 24px;
        max-width: 600px;
        margin: 0 auto;
        display: block;
      }

      .content table {
        width: 100%;
      }

      .clear {
        display: block;
        clear: both;
      }

       /* -= Segment Theming styles -= */

      /* Conventional */
      .CONV.primary {
        background: #ff5e00;
      }
      .CONV.text_primary {
        color: #ff5e00;
        font-family: "Helvetica Neue", "Helvetica", Helvetica, Arial, sans-serif;
      }
      
      .CONV.link-secondary {
        color: #ff5e00;
      }
      .CONV.btn.btn-primary {
        background: #ff5e00;
        color: #ffffff;
      }
      .CONV.btn.btn-outlined {
        border-radius: 4px;
        border: 1px solid rgb(255, 94, 0);
        color: #ff5e00;
        background-color: #ffffff !important;
      }
      /* Conventional Gold*/
      .CONV_GOLD.primary {
        background: #aa9157;
      }
      .CONV_GOLD.text_primary {
        color: #aa9157;
        font-family: "Helvetica Neue", "Helvetica", Helvetica, Arial, sans-serif;
      }
      .CONV_GOLD.link-secondary {
        color: #aa9157;
      }
      .CONV_GOLD.btn.btn-primary {
        background: #aa9157;
        color: #ffffff;
      }
      .CONV_GOLD.btn.btn-outlined {
        border: 1px solid rgb(170, 145, 87);
        color: #aa9157;
        background-color: #ffffff !important;
      }
      

      /* Conventional Private*/

      .CONV_PRIVATE.primary {
        background: #808285;
      }
      .CONV_PRIVATE.text_primary {
        color: #808285;
        font-family: "Helvetica Neue", "Helvetica", Helvetica, Arial, sans-serif;
      }
      .CONV_PRIVATE.link-secondary {
        color: #808285;
      }
      .CONV_PRIVATE.btn.btn-primary {
        background: #808285;
        color: #ffffff;
      }
      .CONV_PRIVATE.btn.btn-outlined {
        border: 1px solid rgb(170, 145, 87);
        color: #808285;
        background-color: #ffffff !important;
      }
      
      /*  Neo */
      .NEO.primary {
        background: #ff5e00;
      }
      .NEO.text_primary {
        color: #ff5e00;
        font-family: "Helvetica Neue", "Helvetica", Helvetica, Arial, sans-serif;
      }
      .NEO.link-secondary {
        color: #ff5e00;
      }
      .NEO.btn.btn-primary {
        background: #ff5e00;
        color: #ffffff;
      }
      .NEO.btn.btn-outlined {
        border: 1px solid rgb(255, 94, 0);
        color: #ff5e00;
        background-color: #ffffff !important;
      }
      /* Islamic */
      .ISLAMIC.primary {
        background: #006862;
      }
      .ISLAMIC.text_primary {
        color: #006862;
        font-family: "Helvetica Neue", "Helvetica", Helvetica, Arial, sans-serif;
      }
      .ISLAMIC.link-secondary {
        color: #006862;
      }
      .ISLAMIC.btn.btn-primary {
        background: #006862;
        color: #ffffff;
      }
      .ISLAMIC.btn.btn-outlined {
        border: 1px solid rgb(0, 104, 98);
        color: #006862;
        background-color: #ffffff !important;
      }
      
      /* Islamic Gold*/
      .ISLAMIC_GOLD .primary {
        background: #aa9157;
      }
      .ISLAMIC_GOLD.text_primary {
        color: #aa9157;
        font-family: "Helvetica Neue", "Helvetica", Helvetica, Arial, sans-serif;
      }
      .ISLAMIC_GOLD.link-secondary {
        color: #aa9157;
      }
      .ISLAMIC_GOLD.btn.btn-primary {
        background: #aa9157;
        color: #ffffff;
      }
      .ISLAMIC_GOLD.btn.btn-outlined {
        border: 1px solid rgb(170, 145, 87);
        color: #aa9157;
        background-color: #ffffff !important;
      }
     
      /* Islamic Private*/

      .ISLAMIC_PRIVATE.primary {
        background: #808285;
      }
      .ISLAMIC_PRIVATE.text_primary {
        color: #808285;
        font-family: "Helvetica Neue", "Helvetica", Helvetica, Arial, sans-serif;
      }
      .ISLAMIC_PRIVATE.link-secondary {
        color: #808285;
      }
      .ISLAMIC_PRIVATE.btn.btn-primary {
        background: #808285;
        color: #ffffff;
      }
      .ISLAMIC_PRIVATE.btn.btn-outlined {
        border: 1px solid rgb(170, 145, 87);
        color: #808285;
        background-color: #ffffff !important;
      }
    
      /* Neo  Simple*/

      .NEO_SIMPLE.primary {
        background: #7a2182;
      }
      .NEO_SIMPLE.text_primary {
        color: #7a2182;
        font-family: "Helvetica Neue", "Helvetica", Helvetica, Arial, sans-serif;
      }
      .NEO_SIMPLE.link-secondary {
        color: #7a2182;
      }
      .NEO_SIMPLE.btn.btn-primary {
        background: #7a2182;
        color: #ffffff;
      }
      .NEO_SIMPLE.btn.btn-outlined {
        border: 1px solid rgb(122, 33, 130);
        color: #7a2182;
        background-color: #ffffff !important;
      }
      
      /* Neo  Business*/

      .NEO_BUSINESS.primary {
        background: #1A4198;
      }
      .NEO_BUSINESS.text_primary {
        color: #1A4198;
        font-family: "Helvetica Neue", "Helvetica", Helvetica, Arial, sans-serif;
      }
      .NEO_BUSINESS.link-secondary {
        color: #1A4198;
      }
      .NEO_BUSINESS.btn.btn-primary {
        background: #1A4198;
        color: #ffffff;
      }
      .NEO_BUSINESS.btn.btn-outlined {
        border: 1px solid rgb(26, 65, 152);
        color: #1A4198;
        background-color: #ffffff !important;
      }
      
      

      /* ------
		PHONE
		For clients that support media queries.
		Nothing fancy. 
------- */
      @media only screen and (max-width: 600px) {
        a[class="btn"] {
          display: block !important;
          margin-bottom: 10px !important;
          background-image: none !important;
          margin-right: 0 !important;
        }
      }
    </style>
  </head>

  <body>
    <!-- Based on segment we need to pass the id-->
    <div id="${segment}">
      <!-- HEADER -->
      <table class="head-wrap">
        <tr>
          <td></td>
          <td class="header email-container">
            <div class="content">
              <table>
                <tr>
                  <td><img src="https://contentdelivery.mashreqbank.com/common/full-logo/${segment}.png" width="177" /></td>
                </tr>
              </table>
            </div>
          </td>
          <td></td>
        </tr>
      </table>
      <!-- /HEADER -->

      <!-- BODY -->
      <table class="body-wrap">
        <tr>
          <td></td>
          <td class="email-container bg_white">
            <!-- A Real Hero (and a real human being) -->
            <p style="margin-bottom: 40px">
              <img src="https://contentdelivery.mashreqbank.com/channel/email_images/header-money-transfer-own-accounts.png" width="600" />
            </p>
            <!-- /hero -->
            <div class="content">
              <table>
                <tr>
                  <td>
                    <h1 style="margin-bottom: 32px">
                      Your money transfer was successful
                    </h1>
                    <h3 style="margin-bottom: 24px">
                      <span style="font-weight: normal">Dear</span>
                      ${customerName},
                    </h3>
                    <p style="margin-bottom: 24px">
                      We would like to inform you of a money transfer that has been initiated and ended with success.
                    </p>
                    <ul style="list-style: circle; padding: 0px 19px;">
                      <li style="margin-bottom: 16px">
                        <p><span style="min-width: 200px;display: inline-block;">Transfer type</span><span style="margin-left: 20px; font-weight: bold;">${transferType}</span></p>
                      </li>
                      <li style="margin-bottom: 16px">
                        <p><span style="min-width: 200px;display: inline-block;">From ${sourceOfFund}</span><span style="margin-left: 20px; font-weight: bold;">${maskedAccount}</span></p>
                      </li>
                      <li style="margin-bottom: 16px" >
                        <p>
                          <span style="min-width: 200px;display: inline-block;">To Account</span><span style="margin-left: 20px;font-weight: bold;">${toAccountNumber}</span>
                        </p>
                       
                      </li>
                      <li style="margin-bottom: 16px">
                        <p><span style="min-width: 200px;display: inline-block;">Amount</span><span style="margin-left: 20px;font-weight: bold;">${currency} ${amount}</span></p>
                      </li>
                      <li style="margin-bottom: 16px">
                        <p><span style="min-width: 200px;display: inline-block;">Status</span><span style="margin-left: 20px;font-weight: bold;">${status}</span></p>
                      </li>
                    </ul>

                    <p style="margin-bottom: 40px">
                      This request was initiated from ${bankName} ${channelType}.
                    </p>
                    
                    ${contactHtmlBody}
                   
                  </td>
                </tr>
              </table>
            </div>

            <table
              style="
                width: 600px;
                margin-bottom: 48px;
                background: #f5f6f9;
              "
            >
              <tr>
                <td style="padding: 0; margin: 0">
                  <img
                    src="https://contentdelivery.mashreqbank.com/channel/email_images/money-transfer-own-accounts-bottom-banner.png"
                    width="100%"
                    alt=""
                  />
                </td>
              </tr>
            </table>

            <div class="content">
              <table>
                <tr>
                  <td>
                    <p style="margin-bottom: 16px">
                      Best Regards,<br />
                      Customer Service, ${bankName}
                    </p>
                    <p
                      style="
                        font-size: 14px;
                        color: #6e6e6e;
                        margin-bottom: 40px;
                      "
                    >
                      Disclaimer: This is a system generated email message.
                      <br />
                      For any queries, please contact the Bank.
                    </p>
                  </td>
                </tr>
              </table>
            </div>

            <!-- social & contact -->
            <table class="social" style="padding: 16px 24px" width="100%">
              <tr>
                <td>
                  <img
                    src="https://contentdelivery.mashreqbank.com/common/small-logo/${segment}.png"
                    style="float: left; margin-right: 8px"
                    width="46"
                    alt=""
                  />
                  <span
                    style="
                      margin-top: 4px;
                      font-size: 12px;
                      display: inline-block;
                    "
                    >Copyright © 2020 ${bankName}</span
                  >
                </td>
                <td style="text-align: right">
                  <a
                    href="${facebookLink}"
                    style="
                      text-decoration: none;
                      display: inline-block;
                      margin-left: 16px;
                    "
                    ><img src="https://contentdelivery.mashreqbank.com/common/icons/facebook.png" width="8" alt="" />
                  </a>
                  <a
                    href="${youtubeLink}"
                    style="
                      text-decoration: none;
                      display: inline-block;
                      margin-left: 16px;
                      position: relative;
                      top: -2px;
                    "
                    ><img src="https://contentdelivery.mashreqbank.com/common/icons/youtube.png" width="12" alt="" />
                  </a>
                  <a
                    href="${linkedinLink}"
                    style="
                      text-decoration: none;
                      display: inline-block;
                      margin-left: 16px;
                    "
                    ><img src="https://contentdelivery.mashreqbank.com/common/icons/linkedin.png" width="15" alt="" />
                  </a>
                  <a
                    href="${instagramLink}"
                    style="
                      text-decoration: none;
                      display: inline-block;
                      margin-left: 16px;
                    "
                    ><img src="https://contentdelivery.mashreqbank.com/common/icons/instagram.png" width="18" alt="" />
                  </a>
                  <a
                    href="${twitterLink}"
                    style="
                      text-decoration: none;
                      display: inline-block;
                      margin-left: 16px;
                    "
                    ><img src="https://contentdelivery.mashreqbank.com/common/icons/twitter.png" width="17" alt="" />
                  </a>
                </td>
              </tr>
            </table>
            <!-- /social & contact -->
          </td>
        </tr>
      </table>
    </div>
    <!-- /BODY -->
  </body>
</html>
