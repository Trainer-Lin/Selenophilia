import React from 'react';

import styles from './index.module.scss';

const Component = () => {
  return (
    <div className={styles.container5}>
      <div className={styles.editorialHeader}>
        <p className={styles.discoverYourSound}>Discover Your Sound</p>
        <div className={styles.container}>
          <p className={styles.welcomeBackLuminaUse}>Welcome back, Lumina User.</p>
        </div>
      </div>
      <div className={styles.sectionFeaturedMusic}>
        <div className={styles.container3}>
          <div className={styles.gradient}>
            <div className={styles.button}>
              <div className={styles.buttonShadow}>
                <img
                  src="../image/mn84n7zd-lm3g6hc.svg"
                  className={styles.container2}
                />
              </div>
            </div>
          </div>
        </div>
        <div className={styles.container4}>
          <p className={styles.featuredTrack}>Featured Track</p>
          <p className={styles.celestialEchoes}>Celestial Echoes</p>
          <p className={styles.lumina}>Lumina</p>
        </div>
      </div>
      <div className={styles.sectionReorganizedBe}>
        <div className={styles.autoWrapper}>
          <div className={styles.localMusicSmallCard}>
            <img src="../image/mn84n7zd-4lf54sf.svg" className={styles.overlay} />
            <p className={styles.text}>本地音乐</p>
          </div>
          <div className={styles.personalMusicSmallCa}>
            <img src="../image/mn84n7zd-rp22eoe.svg" className={styles.overlay2} />
            <p className={styles.text}>个人音乐</p>
          </div>
        </div>
        <div className={styles.myPlaylistsProminent}>
          <img src="../image/mn84n7zd-23f9s3y.svg" className={styles.overlay3} />
          <div className={styles.paragraph}>
            <p className={styles.text2}>我的歌单</p>
            <p className={styles.text3}>Access your curated collections</p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Component;
