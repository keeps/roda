/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.captcha.server;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.octo.captcha.service.CaptchaServiceException;

/**
 * @see <a
 *      href="http://forge.octo.com/jcaptcha/confluence/display/general/5+minutes+application+integration+tutorial?showComments=false">JCaptcha
 *      quick integration tutorial</a>
 * 
 */
public class ImageCaptchaServlet extends HttpServlet {

  /**
	 * 
	 */
  private static final long serialVersionUID = 4662308768959146268L;

  private static final Logger logger = Logger.getLogger(ImageCaptchaServlet.class);

  public void init(ServletConfig servletConfig) throws ServletException {

    super.init(servletConfig);

  }

  protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse response) throws ServletException,
    IOException {

    BufferedImage image;
    try {
      // get the session id that will identify the generated captcha.
      // the same id must be used to validate the response, the session id
      // is a good candidate!
      String captchaId = httpServletRequest.getSession().getId();
      // call the ImageCaptchaService getChallenge method
      image = CaptchaServiceSingleton.getInstance().getImageChallengeForID(captchaId, httpServletRequest.getLocale());

    } catch (IllegalArgumentException e) {
      logger.error("Error sending captcha image", e);
      response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
      return;
    } catch (CaptchaServiceException e) {
      logger.error("Error sending captcha image", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      return;
    } catch (Exception e) {
      logger.error("Error sending captcha image", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      return;
    }

    // set response headers
    response.setHeader("Cache-Control", "no-store");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);

    response.setContentType("image/png");

    ServletOutputStream os = response.getOutputStream();

    // Writing the image to output stream
    ImageIO.write(image, "png", os);

    // flush it in the response
    os.flush();
    os.close();

  }
}