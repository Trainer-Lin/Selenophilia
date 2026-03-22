import React from 'react';

import styles from './index.module.scss';

const Component = () => {
  return (
    <div className={styles.row}>
      <div className={styles.header}>
        <p className={styles.title}>Title</p>
        <div className={styles.ellipse160}>
          <img
            src="../image/mmjdujj1-xofpvxu.svg"
            className={styles.iconChevronRightSlim}
          />
        </div>
      </div>
      <div className={styles.carousel}>
        <div className={styles.card}>
          <img src="../image/mmjdujj2-vqkjv61.png" className={styles.image} />
          <div className={styles.info}>
            <p className={styles.brand}>Brand&nbsp;</p>
            <p className={styles.productName}>Product name</p>
            <p className={styles.a1099}>$10.99</p>
          </div>
        </div>
        <div className={styles.card}>
          <img src="../image/mmjdujj2-1dp31e5.png" className={styles.image} />
          <div className={styles.info}>
            <p className={styles.brand}>Brand&nbsp;</p>
            <p className={styles.productName}>Product name</p>
            <p className={styles.a1099}>$10.99</p>
          </div>
        </div>
        <div className={styles.card}>
          <img src="../image/mmjdujj2-shjizy7.png" className={styles.image} />
          <div className={styles.info}>
            <p className={styles.brand}>Brand&nbsp;</p>
            <p className={styles.productName}>Product name</p>
            <p className={styles.a1099}>$10.99</p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Component;
